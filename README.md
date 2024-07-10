
# WardrobeBuddy Android App
My university project in which I got a first class honors.

## Overview
WardrobeBuddy is an Android application designed to manage and analyze your wardrobe effectively. This app leverages Firebase for user authentication and database services, uses Machine Learning (ML Kit and OpenAI) to process and categorize clothing items, and interacts with a custom backend service hosted on AWS EC2 for product information retrieval.

## Features
- **User Authentication**: Utilizes Firebase Authentication for managing user sessions.
- **Clothing Item Management**: Users can add, view, and delete clothing items from their wardrobe using the Firebase Realtime Database.
- **Image Processing**: Integrates Google's ML Kit and OpenAI's vision models to analyze clothing tags from images, extracting details such as brand, size, price, and article number.
- **Data Storage**: Images and item details are stored and retrieved from Firebase Realtime Database.
- **Product Information**: Connects to a custom API hosted on an AWS EC2 instance to fetch detailed product information.
- **Interactive UI**: Offers a responsive user interface including RecyclerViews for item display, AlertDialogs for user interactions, and activities for detailed views.

## Key Components
### MainActivity
This activity manages the primary user interface where users can add and view their scanned items. It handles user authentication states, displays a list of items, and provides options to capture or select images for processing.

### CollectionDetailActivity
Displays detailed information about a specific collection of items, including total price calculations and options to add more items to the collection.

### OpenAiHelper
Interacts with OpenAI's API to analyze images encoded in Base64. It sends requests to OpenAI's endpoint and processes the response to extract relevant clothing item details.

### APIServiceInterface
Defines the Retrofit interface for HTTP requests to fetch product information from a custom backend. Currently points to a local server but can be updated to hit an AWS EC2 server for live environments.

```java
public interface APIServiceInterface {
    @GET("/fetch-product-info/")
    Call<ProductInfoResponse> fetchProductInfo(
            @Query("product_url") String productUrl,
            @Query("brand") String brand
    );
}
```
### Retrofit Configuration (ItemDetailActivity)
Configured to connect to a local server for development. Replace the base URL with your EC2 server IP for deployment.
```java
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8000") // Localhost IP for Android emulator
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

## Setup
1. Clone the repository.
2. Open the project in Android Studio.
3. Ensure Firebase is configured with your project details.
4. Update the `API_KEY` in `OpenAiHelper` and the base URL in `Retrofit` setup per your deployment.
5. Run the code inside WardrobeBuddy_Scrape_server using the command `uvicorn endpoint:app --reload` to run it locally, however, there is a EC2 server running this code but as this server has limited FREE user resources PLEASE DO NOT use it. `.baseUrl("http://3.253.82.87:80")`. replace the retrofit in the `ItemDetailActivity.java` to use the server.
7. Build and run the application.

## Dependencies
- Firebase Auth and Database
- Google ML Kit
- OpenAI API
- Retrofit
- Glide for image loading

## Contribution
Feel free to fork this repository and contribute by submitting pull requests. Ensure you follow the existing coding standards and add appropriate tests for new features.

## License
Distributed under the MIT License. See `LICENSE` for more information.

---

![image](https://github.com/StephenHughesUni/WardrobeBuddy/assets/74723672/d9a81b64-5bb7-4697-9132-8ce663c6e8f6)
![image](https://github.com/StephenHughesUni/WardrobeBuddy/assets/74723672/769582af-d24c-44cd-8d9e-aad6f174a697)
![image](https://github.com/StephenHughesUni/WardrobeBuddy/assets/74723672/da1e7fa6-c7e1-467f-9e20-547391933718)

