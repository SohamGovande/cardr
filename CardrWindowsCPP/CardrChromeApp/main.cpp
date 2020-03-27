#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <time.h>
#include <string>
#include <fstream>
#include <iostream>
#include <cstdlib>
#include <io.h>
#include <Windows.h>
#include <Shlobj.h>
#include "json.hpp"

using json = nlohmann::json;

// Define union to read the message size easily
typedef union {
	unsigned long u32;
	unsigned char u8[4];
} U32_U8;

// On Windows, the default I / O mode is O_TEXT.Set this to O_BINARY
// to avoid unwanted modifications of the input / output streams.
int SetBinaryMode(FILE* file)
{
	int result;

	result = _setmode(_fileno(file), _O_BINARY);
	if (result == -1)
	{
		perror("Cannot set mode");
		return result;
	}
	// set do not use buffer
	result = setvbuf(file, NULL, _IONBF, 0);
	if (result != 0)
	{
		perror("Cannot set zero buffer");
		return result;
	}

	return 0;
}

// main logic
int main(int argc, char** argv)
{
	if (SetBinaryMode(stdin) != 0)
	{
		return -1;
	}
	if (SetBinaryMode(stdout) != 0)
	{
		return -1;
	}

	size_t iSize = 0;
	U32_U8 lenBuf;
	lenBuf.u32 = 0;
	char* jsonMsg = NULL; // a json message encoded in utf-8 

	iSize = fread(lenBuf.u8, 1, 4, stdin);
	if (iSize == 4)
	{
		int iLen = (int)lenBuf.u32;
		// now read the message
		if (iLen > 0)
		{
			srand(time(0));

			jsonMsg = (char*)malloc(8 * iLen);
			iSize = fread(jsonMsg, 1, iLen, stdin);
			fwrite(lenBuf.u8, 1, 4, stdout);
			fwrite(jsonMsg, 1, iLen, stdout);
			fflush(stdout);

			std::ofstream out("CardifyChromeAppLog.txt");
			out << "Cardify Chrome App - Last Updated v1.2.0" << std::endl;

			std::string jsonString(jsonMsg, iSize);
			out << "Received data '" << jsonString << "'" << std::endl;
			json parsed = json::parse(jsonString.begin(), jsonString.end());

			std::string dumped = parsed.dump();
			U32_U8 dumpedSize;
			dumpedSize.u32 = dumped.size();

			fwrite(dumpedSize.u8, 1, 4, stdout);
			fwrite(dumped.c_str(), 1, dumpedSize.u32, stdout);
			fflush(stdout);


			out << "Parsed json." << std::endl;
			std::string url = parsed["url"].get<std::string>();
			out << "Extracted url." << std::endl; 
			std::string selection = parsed["selection"].get<std::string>();
			out << "Extracted selection." << std::endl; 
			std::string html = parsed["html"].get<std::string>();
			out << "Extracted HTML." << std::endl;

			out << "Creating ID..." << std::endl;
			std::string id = std::to_string(rand() % 1000);
			out << "Created ID " << id << std::endl;
			std::string selectionFilepath = "CardifySelection-" + id + ".txt";
			out << "Created selection path " << selectionFilepath << std::endl;
			std::string htmlFilepath = "CardifyPage-" + id + ".html";
			out << "Created html path " << htmlFilepath << std::endl;

			out << "Writing selection data to file " << selectionFilepath << std::endl;
			out << "Writing html data to file " << htmlFilepath << std::endl;

			std::ofstream selectionFile;
			selectionFile.open(selectionFilepath);
			selectionFile << selection;
			selectionFile.close();

			std::ofstream htmlFile;
			htmlFile.open(htmlFilepath);
			htmlFile << html;
			htmlFile.close();

			TCHAR userHome[MAX_PATH];
			if (SUCCEEDED(SHGetFolderPathA(NULL, CSIDL_PROFILE, NULL, 0, userHome))) {
				out << "Detected user home " << userHome << std::endl;

				std::string command =  userHome + std::string("\\AppData\\Local\\CardifyLauncher\\CardifyLauncher.exe \"") + url + "\" " + id;
				out << "Running system command: " << command << std::endl;

				system(command.c_str());
			} else {
				out << "Unable to get user home directory" << std::endl;
			}

			out.close();
		}

		// free resource
		if (jsonMsg != NULL)
			free(jsonMsg);
	}
}
