package com.ronocod.rickroulette.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * Manages "Authentication" to the backend service.  The SyncAdapter framework
 * requires an authenticator object, so syncing to a service that doesn't need authentication
 * typically means creating a stub authenticator like this one.
 * This code is copied directly, in its entirety, from
 * http://developer.android.com/training/sync-adapters/creating-authenticator.html
 * Which is a pretty handy reference when creating your own syncadapters.  Just sayin'.
 */
public class RickAuthenticator extends AbstractAccountAuthenticator {

    public RickAuthenticator(Context context) {
        super(context);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException("No properties to edit");
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType,
                             String authTokenType,
                             String[] requiredFeatures,
                             Bundle options) throws NetworkErrorException {
        // Because we're not actually adding an account to the device, just return null.
        return null;
    }


    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse response,
            Account account,
            Bundle options) throws NetworkErrorException {
        // Ignore attempts to confirm credentials
        return null;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType,
            Bundle options) throws NetworkErrorException {
        // Getting an authentication token is not supported
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // Getting a label for the auth token is not supported
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType, Bundle options) throws NetworkErrorException {
        // Updating user credentials is not supported
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        // Checking features for the account is not supported
        throw new UnsupportedOperationException();
    }
}