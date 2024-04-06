package com.example.wardrobebuddy.com.firebaseauth;

public class UrlBuilder {

    public static String getUrl(String brand, String articleNumber, String category) {
        String encodedArticleNumber = articleNumber.replace("/", "%2F");

        switch (brand.toLowerCase()) {
            case "zara":
                // Construct the URL based on the category
                return constructZaraUrl(encodedArticleNumber, category);
            case "h&m":
                // Construct the URL for H&M without considering category
                return "https://www2.hm.com/en_ie/search-results.html?q=" + encodedArticleNumber;
            case "cos":
                // Construct the URL for COS without considering category
                return "https://www.cos.com/en_eur/search.html?q=" + encodedArticleNumber;
            // Add more cases for different brands here
            default:
                // Fallback URL (could be a search engine)
                return "https://www.google.com/search?q=" + brand + "+" + encodedArticleNumber;
        }
    }

    private static String constructZaraUrl(String encodedArticleNumber, String category) {
        switch (category.toLowerCase()) {
            case "men":
                return "https://www.zara.com/ie/en/search?searchTerm=" + encodedArticleNumber + "&section=MAN";
            case "women":
                return "https://www.zara.com/ie/en/search?searchTerm=" + encodedArticleNumber + "&section=WOMAN";
            case "kids":
                return "https://www.zara.com/ie/en/search?searchTerm=" + encodedArticleNumber + "&section=KIDS";
            default:
                // Default to the women's section if category is not recognized
                return "https://www.zara.com/ie/en/search?searchTerm=" + encodedArticleNumber + "&section=WOMAN";
        }
    }
}
