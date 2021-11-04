# Scanisette-Community

## Scanner de périphérique de stockage usb pour les virus

Licensed Under GNU AGPLv3

## Credit

Thanks to @fjubin (Me) for https://github.com/fjubin/Scanisette-Community for this first version of the project. I am
like Lucky Luke, a lonesome old coder, more than 25 years now, and i allready need help on github and docker :)

Thanks to Hugo Simancas from data-expertise.com for support me in hard time coding and resolve trouble with antivirus
software, a great guy

Thanks for the coders who expose their codes and who help me with some libs or other softwares
Quartz : http://www.quartz-scheduler.org/  
StreamGobbler : i don't know this technic before this project to thank for the creator SceneBuilder : Nice software to
make javaFX scene  
Regex test : https://regex101.com/r/fVTQjy/4/

And thanks for the Java creator. I remember my computer studies in University of Orsay (26 years ago in 2021).  
I was a physician and change for computer sciences.  
I have headeaches with the POO. I was coding on Java v1.0, and my forum software (client-server + network + socket) was
a big shit, i remember.  
But i would have my diploma, so i take one whole day to review my lessons and one night to recode my app. And i take a
14/20, i learn three or four things with you this day and night :

- coding is a creative task
- coding need some knowledges to take the good way
- Java was great and even if i code in another languages (c#, php, python, node..), Java will already be in my heart
- And have a good communication skill with passion already save you from hard situation Thanks Guys

## What i learned with this project

- There is no common interface on antivirus software to recover the result of a scan, it's crazy
- Multithreading is complex to manage
- Java + JavaFx is cross platform, but for the software i used it's not, so i need some modification for a linux version

## Further information

Refer to the movies in /doc to have some help to start

# External software used

Java program  
openjdk 17 : https://jdk.java.net/17/

JavaFX (i love that UI kit, long life to JavaFX, Scene is like theater, you just raise the curtain)  
openjfx 17 : https://gluonhq.com/products/javafx/

A good antivirus software  
ClamaV : https://www.clamav.net/

To have some information on USB media (a really great tool)
USBDeview (c)2006 - 2021 Nir Sofer : https://www.nirsoft.net/utils/usb_devices_view.html

To test if there is some encrypted file   
7-zip : https://www.7-zip.fr/

And the binding for java  
7-Zip-JBinding : http://sevenzipjbind.sourceforge.net/

## Build yourself

```bash
$ git clone https://github.com/fjubin/Scanisette-Community.git
```

Open with intellij, set jdk, jfx libs, dependencies and compile

## Running

You need a strutured folder like this  
/project-name/  
---scanisette.bat  (with the good paths set for jdk 17 and jfx17)
---scanisette.jar  
---/resources/  
------config.xml  
------yourlogo.png

## Antivirus

This software is provided with the command output parser for ClamaV. We use the flag to not clean the usb media so it's
a automatic alerting software and this software doesn't remove any file.  
You can change the command parameter to have an automatic cleaning by the antivirus software.

### Add an antivirus

You can add another antivirus by :  
-- duplicate the Antivirus ClamaV section in config.xml   
-- and change the values (command, regex for parsing the output)  
-- duplicate the class of Clamav (ClamavAntivirus) that parse the output  
-- and change the parameters of the return of the regexp  







