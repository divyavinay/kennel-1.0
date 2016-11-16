package com.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Divya on 11/15/2016.
 */

public class LoginResult {

    public String verificationPassed;

    public void setVerificationPassed(String verificationPassed) {
        this.verificationPassed = verificationPassed;
    }

    public static LoginResult fromJSON(JSONObject jsonObject)
    {
        LoginResult loginResult = new LoginResult();
        try
        {
            loginResult.verificationPassed = jsonObject.getString("verificationPassed");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        return loginResult;
    }
}
