package com.mdevsolutions.cc2564.Activities;

import java.util.ArrayList;

/**
 * Created by Michi on 21/05/2017.
 */
interface PermissionResultCallback
{
    void PermissionGranted(int request_code);
    void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions);
    void PermissionDenied(int request_code);
    void NeverAskAgain(int request_code);
}