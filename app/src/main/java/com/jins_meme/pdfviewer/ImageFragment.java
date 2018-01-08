package com.jins_meme.pdfviewer;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  ImageFragment.
 *
 *  The MIT License
 *  Copyright 2017 JINS Corp.
 */
public class ImageFragment extends Fragment {

    public static final String BUNDLE_PAGE_NO = "PageNo";

    int mPageNo;
    Bitmap mBitmap;
    ImageView mainImageView;
    ViewTreeObserver vto;

    public static ImageFragment newInstance(int pageNo) {
        ImageFragment f = new ImageFragment();
        Bundle b = new Bundle();
        b.putInt(BUNDLE_PAGE_NO, pageNo);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, null);

        Bundle bundle = getArguments();
        mPageNo = bundle.getInt(BUNDLE_PAGE_NO);

        TextView txtPageNo = view.findViewById(R.id.txt_page_no);
        txtPageNo.setText(String.valueOf(mPageNo));

        mainImageView = view.findViewById(R.id.main_image);
        vto = mainImageView.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(listener);

        return view;
    }

    @Override
    public void onStop() {

        //vto.removeOnGlobalLayoutListener(listener);
        super.onStop();
        mainImageView.setImageBitmap(null);
        mBitmap.recycle();
        mBitmap = null;
    }

    private ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {

            if (mBitmap == null && getActivity() != null) {

                PdfRenderer.Page page = ((FullscreenActivity) getActivity()).getPage(mPageNo);

                mBitmap = Bitmap.createBitmap(mainImageView.getWidth(), mainImageView.getHeight(),
                        Bitmap.Config.ARGB_4444);

                page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                mainImageView.setImageBitmap(mBitmap);

                page.close();
            }
        }
    };
}
