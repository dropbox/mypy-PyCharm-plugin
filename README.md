![mypy logo](https://github.com/dropbox/mypy-PyCharm-plugin/blob/master/mypy-logo.png)

# mypy-PyCharm-plugin

The plugin provides a simple terminal to run fast mypy daemon
from PyCharm with a single click or hotkey and easily navigate
through type checking results. The idea of the mypy terminal is
different from the normal PyCharm type checking that highlights
the errors in a current file. The mypy terminal shows errors in
all files in your project (even in those not currently open).
Also mypy provides a bit stricter type checking and is tunable
by various flags and config settings.

![mypy plugin screenshot](https://github.com/dropbox/mypy-PyCharm-plugin/blob/master/mypy-mypy.png)

## Installation

_Note: The plugin is **not** available at the JetBrains Plugins
Repository yet. You have to build and install the plugin manually.
Follow issue [#7](https://github.com/dropbox/mypy-PyCharm-plugin/issues/7)
for updates about the Plugins Repository._

Requirements for building the plugin:

* [Oracle JDK 8](https://www.oracle.com/javadownload)
    * Either `javac` should be available on your `PATH` or `JAVA_HOME`
      environment variable should contain your JDK installation path

Requirements for running the plugin:

* [Mypy](https://github.com/python/mypy)
    * The plugin runs the `mypy` executable to check types

Installation steps:

1. Clone the GitHub repository.

2. Open the cloned directory in your terminal and build it using this
   shell command:

       ./gradlew clean buildPlugin
       
   or on Windows:
   
       gradlew clean buildPlugin
   
   The plugin file `mypy-PyCharm-plugin.zip` will be built in
   `build/distributions`.
    
3. In PyCharm go to Preferences -> Plugins -> Install plugins from disk
   -> Select the plugin file -> Restart PyCharm when prompted.
   
4. After restart you should find the plugin in View -> Tool windows
   -> Mypy terminal.

## Configuration

Normally, plugin should not require any configuration steps. However,
sometimes plugin cannot find `dmypy` command because it doesn't have
the full environment. If the plugin says something like
`/bin/bash: dmypy command not found` when you try to run mypy,
then this is likely the cause. In this case right click in mypy
terminal in PyCharm -> Configure plugin. Then enter the path where
mypy is installed as PATH suffix. If you are using a virtual environment,
this would look like `/my/project/bin`. If necessary, you can also
configure mypy command to use your custom `.ini` file and flags.

## Usage

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

## Contributing

External contributions to the project should be subject to
Dropbox Contributor License Agreement (CLA).

1. Open the repository in IntelliJ 2019.1 or newer via
   File -> Open. IntelliJ will import it as a Gradle project.

2. Set up a project JDK via File -> Project Structure -> Project
   -> Project SDK. JDK 8 or newer is required.

3. Build and run the plugin via a Gradle task `runIde` available
   as View -> Tool Windows -> Gradle -> Tasks -> intellij -> runIde.

--------------------------------
Copyright (c) 2018 Dropbox, Inc.
