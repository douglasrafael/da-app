package br.edu.uepb.nutes.ocariot.uaal_poc.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.auth0.android.jwt.JWT;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents UserAccess object.
 *
 * @author Douglas Rafael <douglas.rafael@nutes.uepb.edu.br>
 * @version 1.0
 * @copyright Copyright (c) 2018, NUTES/UEPB
 */
public class UserAccess implements Parcelable {
    public static final String KEY_SCOPES = "scope";

    private String subject;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expirationDate; // in milliseconds
    private String scopes;

    public UserAccess() {
    }

    public UserAccess(String subject, String accessToken, String refreshToken,
                      String tokenType, long expirationDate, String scopes, int mode) {
        this.subject = subject;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expirationDate = expirationDate;
        this.scopes = scopes;
    }

    public UserAccess(String subject, String accessToken, long expirationDate,
                      String scopes, int mode) {
        this.subject = subject;
        this.accessToken = accessToken;
        this.expirationDate = expirationDate;
        this.scopes = scopes;
    }

    public UserAccess(String subject, String accessToken, long expirationDate, String scopes) {
        this.subject = subject;
        this.accessToken = accessToken;
        this.expirationDate = expirationDate;
        this.scopes = scopes;
    }

    protected UserAccess(Parcel in) {
        subject = in.readString();
        accessToken = in.readString();
        refreshToken = in.readString();
        tokenType = in.readString();
        expirationDate = in.readLong();
        scopes = in.readString();
    }

    public static final Creator<UserAccess> CREATOR = new Creator<UserAccess>() {
        @Override
        public UserAccess createFromParcel(Parcel in) {
            return new UserAccess(in);
        }

        @Override
        public UserAccess[] newArray(int size) {
            return new UserAccess[size];
        }
    };

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public boolean isExpired() {
        if (accessToken != null && !accessToken.isEmpty())
            return new JWT(accessToken).isExpired(0);

        return true;
    }

    /**
     * Convert object to string in json format.
     *
     * @return String
     */
    public String toJsonString() {
        return String.valueOf(this.toJson());
    }

    /**
     * Convert object to json format.
     *
     * @return String
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Convert json to Object.
     *
     * @param json
     * @return UserAccess
     */
    public static UserAccess jsonDeserialize(String json) {
        Type typeUserAccess = new TypeToken<UserAccess>() {
        }.getType();
        return new Gson().fromJson(json, typeUserAccess);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(subject);
        parcel.writeString(accessToken);
        parcel.writeString(refreshToken);
        parcel.writeString(tokenType);
        parcel.writeLong(expirationDate);
        parcel.writeString(scopes);
    }

    @Override
    public String toString() {
        return "UserAccess{" +
                "subject='" + subject + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expirationDate=" + expirationDate +
                ", scopes=" + scopes +
                '}';
    }

}
