chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {
  if (msg.text === 'getDOM') {
      sendResponse(document.all[0].outerHTML);
  }
});
