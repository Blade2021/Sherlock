##0.3.7
!topic is now an available command to pull the triggering channels topic.

## 0.3.2
Added failure handling to retrieve archive channel data.

## 0.3.1
Fixed archive channel permissions.  Sets overrides appropriately.

## 0.3.0
Archive channels functionality

## 0.2.1
Update the lookup command to provide more clear data.  

## 0.2.0
Major changes to the command system.  Commands now support aliases and individual ranking is set from the command data file.

## 0.1.8
Bug fixes for channel cooldown and automatic unmute functions

## 0.1.7
In order to keep the terminology consistent as more releases are to come, We have switched the "AssignableRoles" to "SelfRoles" to provide a better fit for naming.  Please review the command changes below:

**BEFORE**  

    !assign - Add a role that can be self assigned with a trigger
    !resign - Remove a role that can be self assigned.
    !getaRoles - Get a list of self assignable roles.

**AFTER**  

    !addSelfRole - Add a role that can be self assigned with a trigger
    !removeSelfRole - Remove a role that can be self assigned.
    !getSelfRoles - Get a list of self assignable roles.


## 0.1.6
Channel exceptions have been added.  Channel exceptions will be utilized for controlling what channels will be monitored/filtered using the various filters that are available through Sherlock.  Currently, the only filter that reads this setting, is the embed filter.  

**New Commands:**  

    !addException - Add a channel to the exception list.  
    !removeException - Remove a channel from the exception list.  
    !getExceptions - Get a list of all the current channel exceptions  


## 0.0.5
Introducing mod roles!

**New Commands:**  

    !addModRole - Designate a new role as a mod role with permissions.  
    !removeModRole - Remove a mod role.  
    !updateModRole - Update an existing mod role.  
    !getModRoles - Get a list of current mod roles.  

## 0.0.4
Welcome message automations and settings.

**New Commands:**  

    !welcomeMessage - Get/Set your welcome message to send to users.  (Supports REGEX)  
    !welcomeMethod - How the message will be sent or disabled entirely.  
    !welcomeChannel - Get/Set a custom welcome channel instead of the default channel.  (Default channel is set by your server settings)  
    !welcomeTimeout - Set the message to automatic deletion after x amount of seconds

## 0.0.3
Channel cooldown features.  SQL Bug fixes.  Generic commands added.  

**New Commands:**  

    !embedFilter - Set or Get the Embed Filter Level  
    !cooldown  - Set or Reset a channel cooldown for a textchannel in a guild  
    !info - Get info about Sherlock  
    !ginfo - Get info about the guild  
    !commands - Get a link to the Sherlock commands wiki page

## 0.0.2
Introduced Embed filtering, and added insert/removal methods for language filtering.
  
**New Commands:**  

    !lfadd - Add a badword to the database  
    !lfremove - Remove a badword from the database  
    !lflist - List all currently being watched badwords


## 0.0.1
Initial release, basic features
