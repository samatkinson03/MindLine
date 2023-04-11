**Mobile Development 2022/23 Portfolio**
# API description

Student ID: `C21054763`

_Complete the information above and then write your 300-word API description here.__

In the development of my Android application, MindLine, I utilized several Android APIs to provide a robust and seamless user experience.

Firstly, I leveraged the Room Persistence Library to manage the local SQLite database for my app. By using DAOs, I could easily define and interact with my database tables and provide LiveData objects for observing changes in the database.

To handle user authentication, I integrated the Google Sign-In API and the Firebase Auth API. This allowed users to securely sign in to their accounts and access their data.

For displaying images, I utilized the Glide image loading library, which provides efficient and memory-safe image loading and caching, ensuring fast and smooth scrolling performance in my timeline and detail screens.

In addition, I used the Navigation component of the Android Jetpack library to handle fragment-based navigation within my app, allowing me to define and navigate between different app screens through a visual editor.

To handle date formatting and parsing, I developed a custom utility class that leverages the SimpleDateFormat class to convert between date strings and Date objects. This class also handles fallback parsing in case of errors with the initial format.

I did not use Firebase Storage API, as I decided to use the Android device's internal storage to store images locally. This allowed me to have more control over the storage and ensure that the app was not reliant on an external storage service.

Overall, by using these APIs, I was able to develop a robust and efficient foundation for my app, while also providing a seamless and user-friendly experience for my users. The choices I made were based on my app's requirements and the specific features I wanted to implement. For example, I chose the Google Sign-In API for authentication because it provided a secure and reliable authentication process.
