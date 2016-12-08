football manager (mac)


1) edit your .profile to export:

```
COREFABRIC_ENV=mac
COREFABRIC_OAUTH2_GOOGLE_CLIENT_ID=x
COREFABRIC_OAUTH2_GOOGLE_CLIENT_SECRET=x
```

(nb. the actual OAUTH2 creditianls aren't yet used but some scripts
error as they're an active TODO on the parent project.)


1.999) did git get your submodules?

```
awfulhak:football-manager ben$ git submodule sync
Synchronizing submodule url for 'tools/dependencies/jep'
Synchronizing submodule url for 'tools/dependencies/libtool'
Synchronizing submodule url for 'tools/dependencies/openssl'
Synchronizing submodule url for 'tools/dependencies/protobuf'
awfulhak:football-manager ben$ git submodule init
awfulhak:football-manager ben$ git submodule update
```


2) install the build tools (sorry, it needs protoc at a specific version
for now due to unforeseen dependencies we're not yet using.)

```
awfulhak:football-manager ben$ ./tools/dependencies/clean.sh && ./tools/dependencies/install.sh 
```

nb. for some reason that command line fails on the first go but succeeds on the 2nd and
subseqent.  we still have issues with this stage on our CI server.  c'est la vie & hacked
for now.

3) wow.  the coffee was nice.  did that work?  if it didn't then rethink the gnu project's
successes -- build hell stems from divergence from /bin/sh scripting standards.  but thank
them for a platform-neutral make system when it does, $vendor never agreed that one properly. :)


```
awfulhak:football-manager ben$ ./.local/bin/protoc --version
libprotoc 3.1.0
```

4) ok did you install latest & greatest LTS nodejs?  if not a diversion for you...  we have
an angular2 ui :)

```
awfulhak:football-manager ben$ node --version
v6.2.2
```

5) build static angular2 website resources by compiling typescript etc then build our
target executable jar file embedding all the goodies we can find on the internets which
has a single config file to start it per node and can be deployed in various interesting
cluster topologies.

oh and then run it.

```
(cd a2/ && ./build.sh) && ./gradlew runShadow
...
INFO: ONLINE, press ^C to exit.
```

last line indicates success...  on success please browse http://localhost:1080/ :-)
