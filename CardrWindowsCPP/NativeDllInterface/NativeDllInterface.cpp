#include "stdafx.h"
#include "NativeDllInterface.h"
#include <jni.h>
#include <string>
#include <vector>
#include <iostream>
#include <locale>
#include <codecvt>
#include <algorithm>

struct MSWordWindow
{
	std::string windowName;
	HWND hwnd;

	inline MSWordWindow()
		: windowName(), hwnd(NULL)
	{}

	inline MSWordWindow(const MSWordWindow& other)
		: hwnd(other.hwnd), windowName(other.windowName) {}

	inline MSWordWindow(MSWordWindow&& other)
		: hwnd(other.hwnd), windowName(std::move(other.windowName))
	{
		other.hwnd = NULL;
	}
};

std::vector<MSWordWindow> g_MSWordWindows;

BOOL CALLBACK EnumWindowsProc(HWND hwnd, LPARAM lParam);

JNIEXPORT jobjectArray JNICALL Java_me_sohamgovande_cardr_platformspecific_WinMSWordInteractor_getWordWindows
(JNIEnv* envPtr, jobject obj)
{
	g_MSWordWindows.clear();
	JNIEnv& env = *envPtr;
	EnumWindows(&EnumWindowsProc, NULL);

	jobjectArray jstrListWindowNames = env.NewObjectArray(g_MSWordWindows.size(), env.FindClass("java/lang/String"), env.NewStringUTF(""));

	for (size_t i = 0; i < g_MSWordWindows.size(); i++)
	{
		const MSWordWindow& window = g_MSWordWindows[i];

		env.SetObjectArrayElement(jstrListWindowNames, i, env.NewStringUTF(window.windowName.c_str()));
	}

	return jstrListWindowNames;
}

JNIEXPORT jboolean JNICALL Java_me_sohamgovande_cardr_platformspecific_WinMSWordInteractor_selectWordWindow
(JNIEnv* env, jobject object, jstring jstrWindowTitle)
{
	const char* title = env->GetStringUTFChars(jstrWindowTitle, 0);
	auto it = std::find_if(g_MSWordWindows.begin(), g_MSWordWindows.end(), [title](const MSWordWindow& win) {
		return strcmp(win.windowName.c_str(), title) == 0;
	});
	if (it == g_MSWordWindows.end()) {
		return FALSE;
	}

	SetForegroundWindow(it->hwnd);
	return TRUE;
}

BOOL CALLBACK EnumWindowsProc(HWND hwnd, LPARAM lParam)
{
	constexpr size_t MAX_SIZE = 255;
	TCHAR appClassName[MAX_SIZE];
	GetClassName(hwnd, appClassName, sizeof(appClassName) / sizeof(TCHAR));

	// Is a MS Word window
	if (lstrcmpW(appClassName, L"OpusApp") == 0)
	{
		MSWordWindow win;

		win.windowName.reserve(MAX_SIZE);
		GetWindowTextA(hwnd, &win.windowName[0], MAX_SIZE);
		win.hwnd = hwnd;

		g_MSWordWindows.emplace_back(std::move(win));
	}
	return TRUE;
}

JNIEXPORT void JNICALL Java_me_sohamgovande_cardr_platformspecific_WinMSWordInteractor_setShiftKeyState
(JNIEnv* env, jobject obj, jboolean pressed)
{
	INPUT ip;
	// Set up a generic keyboard event.
	ip.type = INPUT_KEYBOARD;
	ip.ki.wScan = 0; // hardware scan code for key
	ip.ki.time = 0;
	ip.ki.dwExtraInfo = 0;

	// Press the "A" key
	ip.ki.wVk = VK_RSHIFT; // virtual-key code for the shift key
	ip.ki.dwFlags = pressed ? 0 : KEYEVENTF_KEYUP; // 0 for key press, otherwise key release
	SendInput(1, &ip, sizeof(INPUT));

}