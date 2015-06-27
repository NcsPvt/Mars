package itc.google.places;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import itc.google.places.exception.GooglePlacesException;

/**
 * Represents a place returned by Google Places API_
 */
public class Place {
    private final List<String> types = new ArrayList<String>();
    private final List<Photo> photos = new ArrayList<Photo>();
    private final List<Review> reviews = new ArrayList<Review>();
    private final List<AddressComponent> addressComponents = new ArrayList<AddressComponent>();
    private final List<AltId> altIds = new ArrayList<AltId>();
    private GooglePlaces client;
    private String placeId;
    private Scope scope;
    private double lat = -1, lng = -1;
    private JSONObject json;
    private String iconUrl;
    private InputStream icon;
    private String name;
    private String addr;
    private String vicinity;
    private double rating = -1;
    private Status status = Status.NONE;
    private Price price = Price.NONE;
    private String phone, internationalPhone;
    private String googleUrl, website;
    private Hours hours;
    private int utcOffset;
    private int accuracy;
    private String lang;

    protected Place() {
    }

    /**
     * Parses a detailed Place object.
     *
     * @param client  api client
     * @param rawJson json to parse
     * @return a detailed place
     */
    public static Place parseDetails(GooglePlaces client, String rawJson) throws JSONException {
        JSONObject json = new JSONObject(rawJson);

        JSONObject result = json.getJSONObject(GooglePlacesInterface.OBJECT_RESULT);

        // easy stuff
        String name = result.getString(GooglePlacesInterface.STRING_NAME);
        String id = result.getString(GooglePlacesInterface.STRING_PLACE_ID);
        String address = result.optString(GooglePlacesInterface.STRING_ADDRESS, null);
        String phone = result.optString(GooglePlacesInterface.STRING_PHONE_NUMBER, null);
        String iconUrl = result.optString(GooglePlacesInterface.STRING_ICON, null);
        String internationalPhone = result.optString(GooglePlacesInterface.STRING_INTERNATIONAL_PHONE_NUMBER, null);
        double rating = result.optDouble(GooglePlacesInterface.DOUBLE_RATING, -1);
        String url = result.optString(GooglePlacesInterface.STRING_URL, null);
        String vicinity = result.optString(GooglePlacesInterface.STRING_VICINITY, null);
        String website = result.optString(GooglePlacesInterface.STRING_WEBSITE, null);
        int utcOffset = result.optInt(GooglePlacesInterface.INTEGER_UTC_OFFSET, -1);
        String scopeName = result.optString(GooglePlacesInterface.STRING_SCOPE);
        Scope scope = scopeName == null ? null : Scope.valueOf(scopeName);

        // grab the price rank
        Price price = Price.NONE;
        if (result.has(GooglePlacesInterface.INTEGER_PRICE_LEVEL))
            price = Price.values()[result.getInt(GooglePlacesInterface.INTEGER_PRICE_LEVEL)];

        // location
        JSONObject location = result.getJSONObject(GooglePlacesInterface.OBJECT_GEOMETRY).getJSONObject(GooglePlacesInterface.OBJECT_LOCATION);
        double lat = location.getDouble(GooglePlacesInterface.DOUBLE_LATITUDE), lng = location.getDouble(GooglePlacesInterface.DOUBLE_LONGITUDE);

        // hours of operation
        JSONObject hours = result.optJSONObject(GooglePlacesInterface.OBJECT_HOURS);
        Status status = Status.NONE;
        Hours schedule = new Hours();
        if (hours != null) {
            boolean statusDefined = hours.has(GooglePlacesInterface.BOOLEAN_OPENED);
            status = statusDefined && hours.getBoolean(GooglePlacesInterface.BOOLEAN_OPENED) ? Status.OPENED : Status.CLOSED;

            // periods of operation
            JSONArray jsonPeriods = hours.optJSONArray(GooglePlacesInterface.ARRAY_PERIODS);
            if (jsonPeriods != null) {
                for (int i = 0; i < jsonPeriods.length(); i++) {
                    JSONObject jsonPeriod = jsonPeriods.getJSONObject(i);

                    // opening information (from)
                    JSONObject opens = jsonPeriod.getJSONObject(GooglePlacesInterface.OBJECT_OPEN);
                    Day openingDay = Day.values()[opens.getInt(GooglePlacesInterface.INTEGER_DAY)];
                    String openingTime = opens.getString(GooglePlacesInterface.STRING_TIME);

                    // if this place is always open, break.
                    boolean alwaysOpened = openingDay == Day.SUNDAY && openingTime.equals("0000") && !jsonPeriod.has(GooglePlacesInterface.OBJECT_CLOSE);
                    if (alwaysOpened) {
                        schedule.setAlwaysOpened(true);
                        break;
                    }

                    // closing information (to)
                    JSONObject closes = jsonPeriod.getJSONObject(GooglePlacesInterface.OBJECT_CLOSE);
                    Day closingDay = Day.values()[closes.getInt(GooglePlacesInterface.INTEGER_DAY)]; // to
                    String closingTime = closes.getString(GooglePlacesInterface.STRING_TIME);

                    // add the period to the hours
                    schedule.addPeriod(new Hours.Period().setOpeningDay(openingDay).setOpeningTime(openingTime)
                            .setClosingDay(closingDay).setClosingTime(closingTime));
                }
            }
        }

        Place place = new Place();

        // photos
        JSONArray jsonPhotos = result.optJSONArray(GooglePlacesInterface.ARRAY_PHOTOS);
        List<Photo> photos = new ArrayList<Photo>();
        if (jsonPhotos != null) {
            for (int i = 0; i < jsonPhotos.length(); i++) {
                JSONObject jsonPhoto = jsonPhotos.getJSONObject(i);
                String photoReference = jsonPhoto.getString(GooglePlacesInterface.STRING_PHOTO_REFERENCE);
                int width = jsonPhoto.getInt(GooglePlacesInterface.INTEGER_WIDTH), height = jsonPhoto.getInt(GooglePlacesInterface.INTEGER_HEIGHT);
                photos.add(new Photo(place, photoReference, width, height));
            }
        }


        // address components
        JSONArray addrComponents = result.optJSONArray(GooglePlacesInterface.ARRAY_ADDRESS_COMPONENTS);
        List<AddressComponent> addressComponents = new ArrayList<AddressComponent>();
        if (addrComponents != null) {
            for (int i = 0; i < addrComponents.length(); i++) {
                JSONObject ac = addrComponents.getJSONObject(i);
                AddressComponent addr = new AddressComponent();

                String longName = ac.optString(GooglePlacesInterface.STRING_LONG_NAME, null);
                String shortName = ac.optString(GooglePlacesInterface.STRING_SHORT_NAME, null);

                addr.setLongName(longName);
                addr.setShortName(shortName);

                // address components have types too
                JSONArray types = ac.optJSONArray(GooglePlacesInterface.ARRAY_TYPES);
                if (types != null) {
                    for (int a = 0; a < types.length(); a++) {
                        addr.addType(types.getString(a));
                    }
                }

                addressComponents.add(addr);
            }
        }

        // types
        JSONArray jsonTypes = result.optJSONArray(GooglePlacesInterface.ARRAY_TYPES);
        List<String> types = new ArrayList<String>();
        if (jsonTypes != null) {
            for (int i = 0; i < jsonTypes.length(); i++) {
                types.add(jsonTypes.getString(i));
            }
        }

        // reviews
        JSONArray jsonReviews = result.optJSONArray(GooglePlacesInterface.ARRAY_REVIEWS);
        List<Review> reviews = new ArrayList<Review>();
        if (jsonReviews != null) {
            for (int i = 0; i < jsonReviews.length(); i++) {
                JSONObject jsonReview = jsonReviews.getJSONObject(i);

                String author = jsonReview.optString(GooglePlacesInterface.STRING_AUTHOR_NAME, null);
                String authorUrl = jsonReview.optString(GooglePlacesInterface.STRING_AUTHOR_URL, null);
                String lang = jsonReview.optString(GooglePlacesInterface.STRING_LANGUAGE, null);
                int reviewRating = jsonReview.optInt(GooglePlacesInterface.INTEGER_RATING, -1);
                String text = jsonReview.optString(GooglePlacesInterface.STRING_TEXT, null);
                long time = jsonReview.optLong(GooglePlacesInterface.LONG_TIME, -1);

                // aspects of the review
                JSONArray jsonAspects = jsonReview.optJSONArray(GooglePlacesInterface.ARRAY_ASPECTS);
                List<Review.Aspect> aspects = new ArrayList<Review.Aspect>();
                if (jsonAspects != null) {
                    for (int a = 0; a < jsonAspects.length(); a++) {
                        JSONObject jsonAspect = jsonAspects.getJSONObject(a);
                        String aspectType = jsonAspect.getString(GooglePlacesInterface.STRING_TYPE);
                        int aspectRating = jsonAspect.getInt(GooglePlacesInterface.INTEGER_RATING);
                        aspects.add(new Review.Aspect(aspectRating, aspectType));
                    }
                }

                reviews.add(new Review().addAspects(aspects).setAuthor(author).setAuthorUrl(authorUrl).setLanguage(lang)
                        .setRating(reviewRating).setText(text).setTime(time));
            }
        }

        // alt-ids
        JSONArray jsonAltIds = result.optJSONArray(GooglePlacesInterface.ARRAY_ALT_IDS);
        List<AltId> altIds = new ArrayList<AltId>();
        if (jsonAltIds != null) {
            for (int i = 0; i < jsonAltIds.length(); i++) {
                JSONObject jsonAltId = jsonAltIds.getJSONObject(i);

                String placeId = jsonAltId.getString(GooglePlacesInterface.STRING_PLACE_ID);
                String sn = jsonAltId.getString(GooglePlacesInterface.STRING_SCOPE);
                Scope s = sn == null ? null : Scope.valueOf(sn);

                altIds.add(new AltId(client, placeId, s));
            }
        }

        return place.setPlaceId(id).setClient(client).setName(name).setAddress(address).setIconUrl(iconUrl).setPrice(price)
                .setLatitude(lat).setLongitude(lng).addTypes(types).setRating(rating).setStatus(status)
                .setVicinity(vicinity).setPhoneNumber(phone).setInternationalPhoneNumber(internationalPhone)
                .setGoogleUrl(url).setWebsite(website).addPhotos(photos).addAddressComponents(addressComponents)
                .setHours(schedule).addReviews(reviews).setUtcOffset(utcOffset).setScope(scope).addAltIds(altIds)
                .setJson(result);
    }

    /**
     * Returns the client associated with this Place object.
     *
     * @return client
     */
    public GooglePlaces getClient() {
        return client;
    }

    /**
     * Sets the {@link itc.google.places.GooglePlaces} client associated with this Place object.
     *
     * @param client to set
     * @return this
     */
    protected Place setClient(GooglePlaces client) {
        this.client = client;
        return this;
    }

    /**
     * Returns the unique identifier for this place.
     *
     * @return id
     */
    public String getPlaceId() {
        return placeId;
    }

    /**
     * Sets the unique, stable, identifier for this place.
     *
     * @param placeId to use
     * @return this
     */
    protected Place setPlaceId(String placeId) {
        this.placeId = placeId;
        return this;
    }

    /**
     * Returns the latitude of the place.
     *
     * @return place latitude
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * Sets the latitude of the place.
     *
     * @param lat latitude
     * @return this
     */
    protected Place setLatitude(double lat) {
        this.lat = lat;
        return this;
    }

    /**
     * Returns the longitude of this place.
     *
     * @return longitude
     */
    public double getLongitude() {
        return lng;
    }

    /**
     * Sets the longitude of this place.
     *
     * @param lon longitude
     * @return this
     */
    protected Place setLongitude(double lon) {
        this.lng = lon;
        return this;
    }

    /**
     * Returns the amount of seconds this place is off from the UTC timezone.
     *
     * @return seconds from timezone
     */
    public int getUtcOffset() {
        return utcOffset;
    }

    /**
     * Sets the amount of seconds this place is off from the UTC timezone.
     *
     * @param utcOffset in seconds
     * @return this
     */
    protected Place setUtcOffset(int utcOffset) {
        this.utcOffset = utcOffset;
        return this;
    }

    /**
     * Returns the {@link itc.google.places.Hours} for this place.
     *
     * @return hours of operation
     */
    public Hours getHours() {
        return hours;
    }

    /**
     * Sets the {@link itc.google.places.Hours} of this place.
     *
     * @param hours of operation
     * @return this
     */
    protected Place setHours(Hours hours) {
        this.hours = hours;
        return this;
    }

    /**
     * Returns true if this place is always opened.
     *
     * @return true if always opened
     */
    public boolean isAlwaysOpened() {
        return hours.isAlwaysOpened();
    }

    /**
     * Returns this Place's phone number.
     *
     * @return number
     */
    public String getPhoneNumber() {
        return phone;
    }

    /**
     * Sets this Place's phone number.
     *
     * @param phone number
     * @return this
     */
    protected Place setPhoneNumber(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Returns the place's phone number with a country code.
     *
     * @return phone number
     */
    public String getInternationalPhoneNumber() {
        return internationalPhone;
    }

    /**
     * Sets the phone number with an international country code.
     *
     * @param internationalPhone phone number
     * @return this
     */
    protected Place setInternationalPhoneNumber(String internationalPhone) {
        this.internationalPhone = internationalPhone;
        return this;
    }

    /**
     * Returns the Google PLus page for this place.
     *
     * @return plus page
     */
    public String getGoogleUrl() {
        return googleUrl;
    }

    /**
     * Sets the Google Plus page for this place.
     *
     * @param googleUrl google plus page
     * @return this
     */
    protected Place setGoogleUrl(String googleUrl) {
        this.googleUrl = googleUrl;
        return this;
    }

    /**
     * Returns the website of this place.
     *
     * @return website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Sets the website url associated with this place.
     *
     * @param website of place
     * @return this
     */
    protected Place setWebsite(String website) {
        this.website = website;
        return this;
    }

    /**
     * Returns the "vicinity" the place is in. This is sometimes a substitute for address.
     *
     * @return vicinity
     */
    public String getVicinity() {
        return vicinity;
    }

    /**
     * Sets the "vicinity" the place is in. This is sometimes a substitute for address.
     *
     * @param vicinity of place
     * @return this
     */
    protected Place setVicinity(String vicinity) {
        this.vicinity = vicinity;
        return this;
    }

    /**
     * Returns the url of the icon to represent this place.
     *
     * @return icon to represent
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * Sets the url of the icon to represent this place.
     *
     * @param iconUrl to represent place.
     * @return this
     */
    protected Place setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    /**
     * Downloads the icon to this place.
     *
     * @return this
     */
    public Place downloadIcon() {
        icon = client.download(iconUrl);
        return this;
    }

    /**
     * Returns the input stream of this place. {@link #downloadIcon()} must be called previous to call this.
     *
     * @return input stream
     */
    public InputStream getIconInputStream() {
        return icon;
    }

    /**
     * Returns the icon image. {@link #downloadIcon()} must be called previous to this.
     *
     * @return image
     */
    public Bitmap getIconImage() {
        try {
            return BitmapFactory.decodeStream(icon);
        } catch (Exception e) {
            throw new GooglePlacesException(e);
        }
    }

    /**
     * Returns the name of this place.
     *
     * @return name of place
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this place.
     *
     * @param name of place
     * @return this
     */
    protected Place setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the address of this place.
     *
     * @return address of this place
     */
    public String getAddress() {
        return addr;
    }

    /**
     * Sets the address of this place.
     *
     * @param addr address
     * @return this
     */
    protected Place setAddress(String addr) {
        this.addr = addr;
        return this;
    }

    /**
     * Adds a collection of address components to this place.
     *
     * @param c components to add
     * @return this
     */
    protected Place addAddressComponents(Collection<AddressComponent> c) {
        this.addressComponents.addAll(c);
        return this;
    }

    /**
     * Returns the address components for this place.
     *
     * @return address components
     */
    public List<AddressComponent> getAddressComponents() {
        return Collections.unmodifiableList(addressComponents);
    }

    /**
     * Adds a collection of photos to this place.
     *
     * @param photos to add
     * @return this
     */
    protected Place addPhotos(Collection<Photo> photos) {
        this.photos.addAll(photos);
        return this;
    }

    /**
     * Returns the photo references for this place.
     *
     * @return photos
     */
    public List<Photo> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    /**
     * Adds a collection of reviews to this place.
     *
     * @param reviews to add
     * @return this
     */
    protected Place addReviews(Collection<Review> reviews) {
        this.reviews.addAll(reviews);
        return this;
    }

    /**
     * Returns this place's reviews in an unmodifiable list.
     *
     * @return reviews
     */
    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    /**
     * Adds a collection of string "types".
     *
     * @param types to add
     * @return this
     */
    protected Place addTypes(Collection<String> types) {
        this.types.addAll(types);
        return this;
    }

    /**
     * Returns all of this place's types in an unmodifiable list.
     *
     * @return types
     */
    public List<String> getTypes() {
        return Collections.unmodifiableList(types);
    }

    /**
     * Adds a collection of {@link itc.google.places.AltId}s.
     *
     * @param altIds to add
     * @return this
     */
    protected Place addAltIds(Collection<AltId> altIds) {
        this.altIds.addAll(altIds);
        return this;
    }

    /**
     * Returns all of this place's alt-ids in an unmodifiable list.
     *
     * @return alt-ids
     */
    public List<AltId> getAltIds() {
        return Collections.unmodifiableList(altIds);
    }

    /**
     * Returns the rating of this place.
     *
     * @return rating
     */
    public double getRating() {
        return rating;
    }

    /**
     * Sets the rating of this place.
     *
     * @param rating of place
     * @return this
     */
    protected Place setRating(double rating) {
        this.rating = rating;
        return this;
    }

    /**
     * Returns the {@link itc.google.places.Status} of this place.
     *
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the {@link itc.google.places.Status} of this place.
     *
     * @param status to set
     * @return this
     */
    protected Place setStatus(Status status) {
        this.status = status;
        return this;
    }

    /**
     * Returns the {@link itc.google.places.Price} of this place.
     *
     * @return price
     */
    public Price getPrice() {
        return price;
    }

    /**
     * Sets the {@link itc.google.places.Price} of this place.
     *
     * @param price to set
     * @return this
     */
    protected Place setPrice(Price price) {
        this.price = price;
        return this;
    }

    /**
     * Returns the JSON representation of this place. This does not build a JSON object, it only returns the JSON
     * that was given in the initial response from the server.
     *
     * @return the json representation
     */
    public JSONObject getJson() {
        return json;
    }

    /**
     * Sets the JSON representation of this Place.
     *
     * @param json representation
     * @return this
     */
    protected Place setJson(JSONObject json) {
        this.json = json;
        return this;
    }

    /**
     * Returns the accuracy of the location, expressed in meters.
     *
     * @return accuracy of location
     */
    public int getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the accuracy of the location, expressed in meters.
     *
     * @param accuracy of location
     * @return this
     */
    protected Place setAccuracy(int accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    /**
     * Returns the language of the place.
     *
     * @return language
     */
    public String getLanguage() {
        return lang;
    }

    /**
     * Sets the language of the location.
     *
     * @param lang place language
     * @return this
     */
    protected Place setLanguage(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Returns the scope of this place.
     *
     * @return scope
     * @see itc.google.places.Scope
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Sets the scope of the location.
     *
     * @param scope to set
     * @return this
     * @see itc.google.places.Scope
     */
    protected Place setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Returns an updated Place object with more details than the Place object returned in an initial query.
     *
     * @param params extra params to include in the request url
     * @return a new place with more details
     */
    public Place getDetails(Param... params) {
        return client.getPlaceById(placeId, params);
    }

    @Override
    public String toString() {
        return String.format("Place{id=%s}", placeId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Place && ((Place) obj).placeId.equals(placeId);
    }
}
