![mypy logo](https://github.com/dropbox/mypy-PyCharm-plugin/blob/master/mypy-logo.png)

# mypy-PyCharm-plugin

The plugin provides a simple terminal to run fast mypy daemon
from PyCharm with a single click or hotkey and easily navigate
through type checking results.

![mypy plugin screenshot](https://github.com/dropbox/mypy-PyCharm-plugin/blob/master/mypy-mypy.png)

You can pin the terminal to either side of PyCharm window: click
on window toolbar → Move. The current default is bottom, which
works best if you typically have only a few errors. If you are
working on legacy code with many mypy errors, you may want to use
the ‘left’ or ‘right’ setting. Finally, if you have multiple
monitors you might find the floating mode convenient.

Currently supported features and keyboard shortcuts:

- Show/hide mypy terminal:  `Ctrl + Shift + X`
- Run mypy type checking:  `Ctrl + Shift + M` or click Run
- Go to error: click on error line, or use `Ctrl + Shift + <arrows>`
  to navigate between errors
- Copy current error: right click → Copy error text,
  or `Ctrl + Shift + C`
- Collapse/expand errors: click on file name in the mypy terminal,
  or `Ctrl + Shift + Enter` when a file name is selected
- Sometimes mypy shows links to online documentation; to follow
  links use `Alt + <click>`

External contributions to the project should be subject to
Dropbox Contributor License Agreement (CLA).

--------------------------------
Copyright (c) 2018 Dropbox, Inc.
