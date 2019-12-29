#include <Windows.h>

int main()
{
	// Hide the console
	ShowWindow(GetConsoleWindow(), SW_HIDE);
	FreeConsole();

	STARTUPINFO info = { sizeof(info) };
	PROCESS_INFORMATION processInfo;
	ZeroMemory(&processInfo, sizeof(processInfo));
	ZeroMemory(&info, sizeof(info));

	char* command = _strdup("java -jar -Djava.library.path=\"dlls\" CardifyDebate.jar");

	if (CreateProcessA(NULL, command, NULL, NULL, TRUE, CREATE_NO_WINDOW, NULL, NULL, &info, &processInfo))
	{
		CloseHandle(processInfo.hProcess);
		CloseHandle(processInfo.hThread);
	}
	delete[] command;
}