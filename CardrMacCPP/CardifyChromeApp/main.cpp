#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <time.h>
#include <string>
#include <fstream>
#include <iostream>
#include <cstdlib>
#include <iostream>
#include "json.hpp"

// Define union to read the message size easily
typedef union {
    unsigned long u32;
    unsigned char u8[4];
} U32_U8;

using json = nlohmann::json;

// main logic
int main(int argc, char** argv)
{
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

            std::string command = "/Applications/CardifyDebate.app/Contents/MacOS/CardifyDebate \"" + url + "\" " + id;
            out << "Running system command: " << command << std::endl;

            system(command.c_str());

            out.close();
        }

        // free resource
        if (jsonMsg != NULL)
            free(jsonMsg);
    }
}
