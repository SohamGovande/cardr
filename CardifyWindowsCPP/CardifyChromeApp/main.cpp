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

			std::string jsonString(jsonMsg, iSize);
			out << "Received data '" << jsonString << "'" << std::endl;
			json parsed = json::parse(jsonString.begin(), jsonString.end());
			std::string url = parsed["url"].get<std::string>();
			std::string selection = parsed["selection"].get<std::string>();
			std::string html = parsed["html"].get<std::string>();

			std::string id = std::to_string(rand() % 1000);
			std::string selectionFilepath = "CardifySelection-" + id + ".txt";
			std::string htmlFilepath = "CardifyPage-" + id + ".html";

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

			std::string command = "..\\CardifyDebate.exe \"" + url + "\" " + id;
			out << "Running system command (v1.2.0): " << command << std::endl;

			system(command.c_str());

			out.close();
		}

		// free resource
		if (jsonMsg != NULL)
			free(jsonMsg);
	}
}
