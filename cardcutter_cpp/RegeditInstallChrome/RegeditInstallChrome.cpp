#include <Windows.h>
#include <iostream>
#include <string>

enum ProgramMode
{
	INSTALL, UNINSTALL
};


constexpr LPCSTR REGISTRY_KEY = "Software\\Google\\Chrome\\NativeMessagingHosts\\me.matrix4f.cardify";

std::string getNativeJsonFile() 
{
	char path[MAX_PATH];
	GetModuleFileName(NULL, path, MAX_PATH);

	// Get the location of the last backslash
	int backslashIndex = 0;
	for (int i = 0; i < MAX_PATH; i++)
		if (path[i] == '\\')
			backslashIndex = i;
	path[backslashIndex+1] = '\0';

	std::string result = path;
	result += "me.matrix4f.cardify.json";
	return result;
}

int install()
{
	HKEY hkResult;
	DWORD disposition;

	LSTATUS result = RegCreateKeyEx(
		HKEY_LOCAL_MACHINE, 
		REGISTRY_KEY, 
		0,
		NULL,
		REG_OPTION_NON_VOLATILE,
		KEY_ALL_ACCESS,
		NULL,
		&hkResult,
		&disposition
	);

	if (result != ERROR_SUCCESS)
	{
		std::cerr << "Unable to open registry key." << std::endl;
		
		return -1;
	}

	if (disposition == REG_CREATED_NEW_KEY) {
		std::cout << "Created new key!" << std::endl;
	} else if (disposition == REG_OPENED_EXISTING_KEY) {
		std::cout << "Opened existing key!" << std::endl;
	} else {
		std::cout << "Invalid disposition: " << disposition << std::endl;
		return -1;
	}


	std::string jsonPath = getNativeJsonFile();

	result = RegSetValueEx(hkResult, NULL, 0, REG_SZ, reinterpret_cast<const BYTE*>(jsonPath.c_str()), jsonPath.size()+1);
	if (result != ERROR_SUCCESS)
	{
		std::cerr << "Unable to set registry key." << std::endl;
		return -1;
	}

	result = RegCloseKey(hkResult);
	if (result != ERROR_SUCCESS)
	{
		std::cerr << "Unable to close registry key." << std::endl;
		return -1;
	}
	return 0;
}

int uninstall()
{
	LSTATUS result = RegDeleteKey(HKEY_LOCAL_MACHINE, REGISTRY_KEY);
	if (result != ERROR_SUCCESS) {
		std::cerr << "Unable to uninstall key!" << std::endl;
	}
	return 0;
}

int main(int argc, char** argv)
{
	ProgramMode mode = INSTALL;
	if (argc == 2)
	{
		mode = UNINSTALL;
	}

	switch (mode)
	{
	case INSTALL:
		return install();
	case UNINSTALL:
		return uninstall();
	}
	return 0;
}