/*  Orderlist Sort must be by last created. Because after creating a new order, that is instantly added to the cached order list at the top
* Book list sort shouldn't matter because it is re retrieved if a new book is added. In case of update, it is added only if it is found
* in the cache list of books and replaced at that particular index. In case of removing a book, it is deleted if it is found.
* The page size must not be more than 10. Because orders are extracted with a limit of 10. Because relational object query in Backendless
* implements paging and the max is 10. You need to reconfigure the query and everything if page size is more than 10. However, for retrieving
* books, there shouldn't be any issue as the book objects are not related. But for consistency it is recommended that both be the same.
* */

package com.example.gronthomongol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserTokenStorageFactory;

import java.util.List;

public class CONSTANTS {
    private static int MAX_NO_OF_BOOKS_PER_USER_PER_ORDER = 10;
    private static int MAX_NO_OF_ORDERS_PER_USER_PER_BOOK = 2;
    private static int MIN_NO_OF_ORDERS_PER_USER_PER_BOOK = 1;
    private static int ID_BOOKLIST = 17;
    private static int ID_SPALSH_SCREEN = 29;
    private static int ID_LOGIN = 31;
    private static int ID_PLACE_ORDER = 19;
    private static int ID_VIEW_ORDERS = 37;
    private static int ID_PLACE_ORDER_ADD_MORE_BOOK = 23;
    private static BackendlessUser currentUser;
    public static List<Book> bookListCached;
    public static List<Order> myOrdersCached;
    private static int OFFSET = 0;      // Explicitly for booklist
    private static int MYORDEROFFSET = 0;
    private static int PAGE_SIZE = 13;
    private static int MY_ORDER_PAGE_SIZE = 13;
    private  static DataQueryBuilder bookListQueryBuilder;
    private  static DataQueryBuilder orderListQueryBuilder;
    public static String NULLMARKER = "N/A_AUTO";

    public static int getOFFSET() {
        return OFFSET;
    }

    public static int getIdPlaceOrderAddMoreBook() {
        return ID_PLACE_ORDER_ADD_MORE_BOOK;
    }

    public static int getIdSpalshScreen() {
        return ID_SPALSH_SCREEN;
    }

    public static int getIdLogin() {
        return ID_LOGIN;
    }

    public static int getMyOrderPageSize() {
        return MY_ORDER_PAGE_SIZE;
    }

    public static BackendlessUser getCurrentUser() {
        return currentUser;
    }

    public static int getIdViewOrders() {
        return ID_VIEW_ORDERS;
    }

    public static void setCurrentUser(BackendlessUser currentUser) {
        CONSTANTS.currentUser = currentUser;
    }

    public static int getMYORDEROFFSET() {
        return MYORDEROFFSET;
    }

    public static void setMYORDEROFFSET(int MYORDEROFFSET) {
        CONSTANTS.MYORDEROFFSET = MYORDEROFFSET;
    }

    public static DataQueryBuilder getOrderListQueryBuilder() {
        return orderListQueryBuilder;
    }

    public static void setOrderListQueryBuilder(DataQueryBuilder orderListQueryBuilder) {
        CONSTANTS.orderListQueryBuilder = orderListQueryBuilder;
    }

    public static int getMaxNoOfBooksPerUserPerOrder() {
        return MAX_NO_OF_BOOKS_PER_USER_PER_ORDER;
    }

    public static int getPageSize() {
        return PAGE_SIZE;
    }

    public static DataQueryBuilder getBookListQueryBuilder() {
        return bookListQueryBuilder;
    }

    public static int getIdBooklist() {
        return ID_BOOKLIST;
    }

    public static int getIdPlaceOrder() {
        return ID_PLACE_ORDER;
    }

    public static void setBookListQueryBuilder(DataQueryBuilder bookListQueryBuilder) {
        CONSTANTS.bookListQueryBuilder = bookListQueryBuilder;
    }

    public static List<Book> getBookListCached() {
        return bookListCached;
    }

    public static void setBookListCached(List<Book> bookListCached) {
        CONSTANTS.bookListCached = bookListCached;
    }

    public static List<Order> getMyOrdersCached() {
        return myOrdersCached;
    }

    public static void setMyOrdersCached(List<Order> myOrdersCached) {
        CONSTANTS.myOrdersCached = myOrdersCached;
    }

    public static int getMAX_NO_OF_ORDERS_PER_USER_PER_BOOK() {
        return MAX_NO_OF_ORDERS_PER_USER_PER_BOOK;
    }

    public static int getMIN_NO_OF_ORDERS_PER_USER_PER_BOOK() {
        return MIN_NO_OF_ORDERS_PER_USER_PER_BOOK;
    }

    public static void setOFFSET(int OFFSET) {
        CONSTANTS.OFFSET = OFFSET;
    }

    public static void freshRetrieveFromDatabase(final Context context, final BooklistAdapterRV booklistAdapterRV, String sortBy, final Dialog dialog,
                                                 final RecyclerView recyclerView, final EndlessScrollEventListener endlessScrollEventListener){
        // Internal Changes
        Log.i("booklist_retrieve", "freshRetrieveFromDatabase: before sortBy = " + sortBy);

        CONSTANTS.setOFFSET(0);
        SharedPreferences pref = context.getSharedPreferences("preferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("sortBy", sortBy);
        editor.commit();


        Log.i("booklist_retrieve", "freshRetrieveFromDatabase: sortBy = " + sortBy);

        // Database Stuff
        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        String whereClause = "quantity > 0";
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy(sortBy);
        queryBuilder.setPageSize( CONSTANTS.getPageSize() ).setOffset( CONSTANTS.getOFFSET() );
        Backendless.Data.of( Book.class ).find( queryBuilder,
                new AsyncCallback<List<Book>>()
                {
                    @Override
                    public void handleResponse( List<Book> response )
                    {
//                        booklistAdapter = new BooklistAdapter(getApplicationContext(), response);
//                        listView.setAdapter(booklistAdapter);

                        CONSTANTS.bookListCached.clear();
                        CONSTANTS.bookListCached.addAll(response);

                        Log.i("booklist_retrieve", "received list[0] = " + response.get(0).getName());
                        Log.i("booklist_retrieve", "cached list[0] = " + CONSTANTS.bookListCached.get(0).getName());

                        endlessScrollEventListener.reset();
                        booklistAdapterRV.notifyDataSetChanged();
                        CONSTANTS.setBookListQueryBuilder(queryBuilder);    // To make this exact querybuilder accessible from all over the app
                        Log.i("booklist_retrieve", "Booklist retrieved. Size = " + response.size());
                        CONSTANTS.setOFFSET(CONSTANTS.getOFFSET()+CONSTANTS.getPageSize());

                        dialog.dismiss();

                    }

                    @Override
                    public void handleFault( BackendlessFault fault )
                    {


                        dialog.dismiss();

                        if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) ))
                            showConnectionFailedDialogWithoutRestart(context);
                        else{
                            Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }

                        Log.e("fault", fault.getMessage());

                        //Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });

    }

    // Will be called from Splash Screen right after login if not Admin
    public static void retrieveMyOrders(){
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        String whereClause = "user = " + currentUser.getObjectId();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setPageSize( CONSTANTS.getPageSize() ).setOffset( CONSTANTS.getOFFSET() );
        Backendless.Data.of(Order.class).find(new AsyncCallback<List<Order>>() {
            @Override
            public void handleResponse(List<Order> response) {
                Log.i("myOrders_retrieve", "handleResponse: My orders retrieved. response size = " + response.size());
                CONSTANTS.setMyOrdersCached(response);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // TODO CONNECTION FAILED DIALOG NOT IMPLEMENTED

                Log.i("myOrders_retrieve", "handleFault: " + fault.getMessage());
            }
        });
    }


    public static void sendNotificationToTheAdmins(String message, String messageTitle) {
        PublishOptions publishOptions = new PublishOptions();
        publishOptions.putHeader( "android-ticker-text", "You just got a private push notification!" );
        publishOptions.putHeader( "android-content-title", messageTitle );
        publishOptions.putHeader( "android-content-text", "Push Notifications Text" );
        Backendless.Messaging.publish("admin", message, publishOptions, new AsyncCallback<MessageStatus>() {
            @Override
            public void handleResponse(MessageStatus response) {
                Log.i("notification", "handleResponse: Notification sent to the admins");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i("notification", "handleFault: Notification sending to the admins failed\t" + fault.getMessage());
            }
        });
    }


    public static void showConnectionFailedDialog(final Context context)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity)context));
        alertDialogBuilder.setTitle("Connection Failed!");
        alertDialogBuilder.setMessage("Please check your internet connection and try again");
        alertDialogBuilder.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        arg0.dismiss();
                        Intent intent = ((Activity)context).getIntent();
                        ((Activity)context).finish();
                        ((Activity)context).startActivity(intent);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void showConnectionFailedDialogWithoutRestart(final Context context)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity)context));
        alertDialogBuilder.setTitle("Connection Failed!");
        alertDialogBuilder.setMessage("Please check your internet connection and try again");
        alertDialogBuilder.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        arg0.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
