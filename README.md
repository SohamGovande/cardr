# cardr
## don't work harder, use cardr.
Cardr is an advanced, next-gen evidence collector for all debaters - https://cardrdebate.com

## Installation

### Installing on Windows

1. Visit [the downloads page](https://cardrdebate.com/download-win.html) and download the latest Cardr installer.
2. Double-click the file to run. If you receive a SmartScreen notification, then click "More Info" and then "Run Anyway". This is just a warning displayed to programs that don't have many downloads yet - Cardr is **not** a virus; you can check our VirusTotal rating [here](https://www.virustotal.com/gui/file/0d1bd1c2fb1520c9853f196c0a3275b169f7f6c7affeb3840dd0d2eb30e62b40/detection).
3. Once Cardr Desktop Client has installed, [get the Chrome extension](https://chrome.google.com/webstore/detail/cardr-the-next-gen-debate/ifdnjffggmmjiammdpklgldliaaempce?hl=en).

You're all good to go!

### Installing on macOS

1. Visit https://cardrdebate.com/download-mac.html and download the latest Cardr installer.
2. Double-click the installer to run it. Follow all the steps.
3. Once installed, you will have to manually launch Cardr at least once. Then, [get the Chrome extension](https://chrome.google.com/webstore/detail/cardr-the-next-gen-debate/ifdnjffggmmjiammdpklgldliaaempce?hl=en).

You're all good to go!

## General Information

Cardr is a piece of software that makes it easier for high-school and collegiate debaters to conduct research and generate citations for evidence.

### Desktop Client

*CardrDesktop/*

Cardr's Desktop Client is a program that creates debate cards for you. Once you paste in a webpage URL, you can easily manipulate attributes of the card (publication date, publisher, author(s), etc.) from the side panel. Cardr will automatically generate the citation, along with the card body, for you.

The Desktop Client was written almost entirely in Kotlin, but some parts use C++ (*CardrWindowsCPP/* and *CardrMacCPP/*) for access to Windows-specific and macOS-specific APIs. 

### Chrome Extension

*CardrExtension/*

The Chrome extension allows you to seamlessly integrate your research experience with the Cardr Desktop Client.
It allows you to "send" a website into the Desktop Client by clicking the extension icon in the Chrome toolbar.

The Chrome extension was written using JavaScript.

### OCR API

*CardrOCR/*

Cardr has a separate OCR API that uses Tess4j to scan text from screenshots.


## Credits
* Soham Govande - Founder & Lead Developer

## Contributions
To make a pull request to the repository, please sign and email the "Cardr Individual Content Licensing Agreement.pdf" to sohamthedeveloper@gmail.com.
