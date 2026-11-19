package com.creatix.neonlock;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

/** Only serves the SHA-256-checked APK inside private application cache. */
public final class UpdateApkProvider extends ContentProvider {
    private static final String PATH="/update.apk";
    private static final String MIME="application/vnd.android.package-archive";

    static File getApkFile(Context context){
        return new File(new File(context.getCacheDir(),"neon_update"),"NEON-LOCK-update.apk");
    }
    static Uri getApkUri(Context context){
        return Uri.parse("content://"+context.getPackageName()+".updates"+PATH);
    }
    private boolean supported(Uri uri){
        return uri!=null && PATH.equals(uri.getPath()) &&
            getContext()!=null && (getContext().getPackageName()+".updates").equals(uri.getAuthority());
    }

    @Override public boolean onCreate(){return true;}
    @Override public String getType(Uri uri){return supported(uri)?MIME:null;}

    @Override public ParcelFileDescriptor openFile(Uri uri,String mode) throws FileNotFoundException{
        if(!supported(uri)||!"r".equals(mode))throw new FileNotFoundException("Invalid update path");
        File file=getApkFile(getContext());
        if(!file.isFile())throw new FileNotFoundException("Update APK missing");
        return ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri,String[] projection,String selection,
                                  String[] selectionArgs,String sortOrder){
        if(!supported(uri))return null;
        File file=getApkFile(getContext());
        String[] columns=projection==null?
            new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;
        MatrixCursor cursor=new MatrixCursor(columns);
        Object[] row=new Object[columns.length];
        for(int i=0;i<columns.length;i++){
            if(OpenableColumns.DISPLAY_NAME.equals(columns[i]))
                row[i]="NEON-LOCK-update.apk";
            else if(OpenableColumns.SIZE.equals(columns[i]))
                row[i]=file.length();
            else row[i]=null;
        }
        cursor.addRow(row);
        return cursor;
    }

    @Override public Uri insert(Uri uri,ContentValues values){
        throw new UnsupportedOperationException("Read only");
    }
    @Override public int delete(Uri uri,String where,String[] arguments){
        throw new UnsupportedOperationException("Read only");
    }
    @Override public int update(Uri uri,ContentValues values,String where,String[] arguments){
        throw new UnsupportedOperationException("Read only");
    }
}
