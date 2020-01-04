#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string>
#include <fstream>
#include <iostream>
#include <io.h>
#include <Windows.h>

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
			jsonMsg = (char*)malloc(8 * iLen);
			iSize = fread(jsonMsg, 1, iLen, stdin);

			std::ofstream out("CardifyChromeAppLog.txt");
			// process message
			char* newstr = new char[iSize + 1 - 10];
			memcpy(newstr, jsonMsg + 8, iSize - 10);
			newstr[iSize - 10] = '\0';
			std::string url(newstr);
			delete[] newstr;

			out << "Extracted URL: " << url << std::endl;

			std::string command = "..\\CardifyDebate.exe \"" + url + "\"";

			out << "Running system command: " <<  command << std::endl;
			out.close();

			fwrite(lenBuf.u8, 1, 4, stdout);
			fwrite(jsonMsg, 1, iLen, stdout);
			fflush(stdout);

			system(command.c_str());
		}

		// free resource
		if (jsonMsg != NULL)
			free(jsonMsg);
	}
}
