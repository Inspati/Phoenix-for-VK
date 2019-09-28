# Phoenix for VK
First open-sourced VK client for Android inspired by Material Design.

<b>Screenshots:</b>
<img src="Screenshots.jpg"/>

<b>Available at Google Play</b><br>
  <b>Full:</b> https://play.google.com/store/apps/details?id=biz.dealnote.phoenix <br>
  <b>Lite:</b> https://play.google.com/store/apps/details?id=biz.dealnote.messenger

<b>Build guide:</b>
Requirements:
  1) Android Studio 3.5 or higher
  2) Android SDK r28
  
<b>Setting up enviroment:</b>
In order to build this project you need to add missing file with several VK keys.

  1) Create "build-config-fields.properties" file in the root of project with data in following format

```
full_vk_app_id=6209567
full_gcm_sender_id=""
full_vk_service_token=""
full_vk_client_secret=""
full_youtube_dev_key=""
lite_vk_app_id=6209567
lite_gcm_sender_id=""
lite_vk_service_token=""
lite_vk_client_secret=""
lite_youtube_dev_key=""
```

You can use one value for Lite and Full version if you wish.

  2) We also need to configure Google Services in order to get push-messages and crash reports. Following files you can find in your Google Play console.<br>
  <b>Create files:</b><br>
    - app/src/lite/google-services.json <br>
    - app/src/full/google-services.json

  All project variables are setup now.

  3) Build APK :)
