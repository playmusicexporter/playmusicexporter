# Play Music Exporter

This Android app exports your Play Music mp3 files directly to your sdcard
or a documents provider using root permissions.
You can also setup automatic export, that exports all currently cached not yet exported music.

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/app/re.jcg.playmusicexporter)

## About

This AndroidStudio project allow you to access the database from Google's PlayMusic
and also allows you to export the music files as mp3 to your sdcard or a documents provider.
There is also a nice library you can simply use in your projects.

### Requirements

**This app and the included library will require root access to your device!
If your device is not rooted you can neither use this app nor the library.**

This app uses API Level 21, which has been introduced by Android Lollipop. If you use KitKat
or lower, this app will not work.

### Credits

This is a fork off [David Schulte's original project](https://github.com/Arcus92/PlayMusicExporter).
Most of the work has been done by him, me and the other collaborators just have improved his work
in some minor ways, like making the app more usable and configurable. The exporting itself, the hard
part, has been done by him.

This project uses the [mp3agic library](https://github.com/mpatric/mp3agic)
by [Michael Patricios (mpatric)](https://github.com/mpatric).

### Contributing

If you want to contribute to this project, fork off of develop,
implement what you want to implement, and submit a pull request back into develop.
After testing it, if enough of the collaborators like it, we will merge it.
You might want to discuss your ideas with us first though:

### Discuss Ideas

If you have any questions, the easiest way to get help is asking in the #playmusicexporter IRC channel on freenode. You can join that on webchat.freenode.net, or preferably, if you are not really an IRC guy anyway, try the corresponding matrix room [#playmusicexporter:jcg.re](https://matrix.to/#/#playmusicexporter:jcg.re). They are bridged together, so everything that happens in one of the rooms gets automatically transfered to the other one too.

### Copyright

Copyright (c) 2016 Jan Christian Grünhage. See LICENSE.txt for details.  
Copyright (c) 2015 David Schulte. See LICENSE.txt for details.
