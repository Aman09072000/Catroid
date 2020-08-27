# Modularizing „Pocket Code“: Using Android modules

The main idea of the project was to split up the two logical parts of the Pocket Code application. 
Since the app can be seen as a game engine, these two parts can be determined as the IDE and the Stage. 
Where the IDE part is responsible for the Navigation through the app and external sources and also the editing of the user’s projects. 
When executing the created projects, the Stage parts comes to its work and tries to execute the selected project. 
To start this kind of bigger project, the first idea was to split up the stage part from the IDE part.

## Android Library Module

First approach was to create an Android library module `-> apply plugin: ‘com.android.library’`. 
A library module is basically the same as an application module `-> apply plugin: ‘com.android.application’`. 
But an application module is a fully standalone able Android application which can be built to an APK. 
A library module is basically code and resources which are getting packed into an AAR File to be (re)used by an Android application module. 
The idea was to create a library module for the Stage part of the Pocket Code application.

### Multi-module version handling

After researching for best practices, first thing was to create a Gradle file which should be used to store version codes for all used packages. 
When using multiple modules and they are all using e.g. libraries like Espresso, the best case should be that each module uses the same versions. 
Since our newly created library module has its own Gralde file, we can also select specific libraries to be only loaded for one module. 
The LibgDx graphics enginge e.g. could be shifted to exist only in Stage module. 
After creating the overall version storage the conversion part of the old application starts.

The created files were: [build.gradle for Stage](https://github.com/DinosaurierRex/Catroid/blob/GSOC-DS-001/Stage/build.gradle) and 
[version.gradle as storage](https://github.com/DinosaurierRex/Catroid/blob/GSOC-DS-001/version.gradle)

### Refactoring: Relevant parts and dependencies between modules
For this task the easiest way to do was to shift all related packages just to the src folder of the newly created module. 
Problem with that is that currently all our code lies in application module. 
Modularizing an app with library modules is basically a one-way ticket. 
An application module can depend on different libraries, but a library cannot depend on an application.  

<img src="librarymodule_picture.png" alt="Library module dependencies" width="450" height="200">

### Solution: Resolving dependencies
When now starting to shift only the packages, you immediately have to change about 50 Files for refactoring the dependencies (mainly renaming!).
These changes can be seen on this [Commit](https://github.com/DinosaurierRex/Catroid/commit/ba4fe8da271a09d09fbcf795d6cd53f53062763a)
Afterwards its clear to see that there are a lot of dependencies going back to the application module. 
Since we are not allowed to have dependencies to our application module there are basically two options:
* Shifting relevant parts also to module: E.g. Classes Sprite, Project, … are used in both parts and are not clearly separable yet.
  * But it’s possible to just shift those classes also to stage module because then we can also use it in application module.
  * If this is done for all classes, we wouldn’t reach our goal to clearly separate Stage and IDE part and would need a refactoring of all issued classes.
* Refactor every related class to don’t have dependencies back to the application module.
  * After talking to Patrick, we tried to shift from outter to core. But even sthe smallest only stage related classes (“Actions” in Pocket Code, over a 100 files) have all dependencies to the application module.
  * Each of these dependencies will result in another file which needs to be refactored to be separate.
  
Example for [StopSoundAction.kt](https://github.com/Catrobat/Catroid/blob/develop/catroid/src/main/java/org/catrobat/catroid/content/actions/StopSoundAction.kt). 
he application module package of Pocket Code is called **org.catrobat.catroid**. 
*Four out of five* `import` statements are relating to application module. When looking at the `import` statetements the dependent classes are clearly visible:  
` ... `  
`import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction`  
`import org.catrobat.catroid.common.SoundInfo`  
`import org.catrobat.catroid.content.Sprite`  
`import org.catrobat.catroid.io.SoundManager`  
`import org.catrobat.catroid.pocketmusic.mididriver.MidiSoundManager`   
` ... `  

