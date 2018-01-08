package com.jins_meme.pdfviewer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *  ImageViewPagerAdapter.
 *
 *  The MIT License
 *  Copyright 2017 JINS Corp.
 */
public class ImageViewPagerAdapter extends FragmentStatePagerAdapter {

    private int mMaxPage;
    private Context mContext;
    private PdfRenderer mPdfRenderer;

    public ImageViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        mContext = context;

    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mMaxPage;
    }

    protected void open(String path){

        try {
            mPdfRenderer = new PdfRenderer(getSeekableFileDescriptor(path));

            mMaxPage = mPdfRenderer.getPageCount();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected PdfRenderer.Page getPage(int p){
       return mPdfRenderer.openPage(p);
    }

    protected ParcelFileDescriptor getSeekableFileDescriptor(String path) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor;

        AssetManager am = mContext.getAssets();
        InputStream inputStream = am.open(path);
        File file = createFileFromInputStream(inputStream);

        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        return parcelFileDescriptor;

    }

    private File createFileFromInputStream(InputStream inputStream) {

        try {
            File f = new File(mContext.getFilesDir(), "tmp.pdf");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            //Logging exception
        }

        return null;
    }

    private boolean isAnAsset(String path) {
        return !path.startsWith("/");
    }

    public void close() {
        mPdfRenderer.close();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }
}
