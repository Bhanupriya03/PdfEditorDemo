package com.hsc.pdfeditor.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PdfToImage extends Activity {

    String TAG = "PdfToImage";
    ArrayList<String> pageImageList = new ArrayList<String>();
    pdfAdapter adapter ;//= new pdfAdapter(this,pageImageList);
    ListView list;
    ProgressDialog progress;
    int pageLimit = 3;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_to_image);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //showPdfToImage();

    }
    private void showPdfToImage() {

        new AsyncTask<Void, Void, ArrayList<String>>()
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new ProgressDialog(PdfToImage.this);
                progress.setTitle(" PDF Images ");
                progress.setMessage("Loading Images");
                progress.setIndeterminate(false);
                progress.show();
            }

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                PdfRenderer renderer = null;
                String file_path = getIntent().getStringExtra("filePath");
                try {
                    renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(file_path), ParcelFileDescriptor.MODE_READ_ONLY));
                    //renderer = new PdfRenderer(getAssets().openFd(pdf_name).getParcelFileDescriptor());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                for(int i=0;i<renderer.getPageCount();i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    Log.d(TAG,"starting bitmap ------->");
                    Bitmap pageImage = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    Log.d(TAG,"Bitmap Created <--------");
                    page.render(pageImage, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    String img_path = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/render_" + i + ".jpg";
                    File renderFile = new File(img_path);
                    try {
                        renderFile.createNewFile();
                        FileOutputStream fileOut = null;
                        fileOut = new FileOutputStream(renderFile);
                        pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                        pageImageList.add(img_path);
                        fileOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    page.close();
                   // page =null;
                    //ImageView imageView = (ImageView) findViewById(R.id.renderedImageView);
                    //imageView.setImageBitmap(pageImage);
                }
                renderer.close();
                return pageImageList;
            }

            @Override
            protected void onPostExecute(ArrayList<String> pageImageList) {
                super.onPostExecute(pageImageList);
                //adapter.notifyDataSetChanged();
                adapter = new pdfAdapter(PdfToImage.this,pageImageList);
                list = (ListView)findViewById(R.id.img_listview);
                list.setAdapter(adapter);
                progress.dismiss();
            }
        }.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        showPdfToImage();
//        adapter = new pdfAdapter(PdfToImage.this,pageImageList);
//        list = (ListView)findViewById(R.id.img_listview);
//        list.setAdapter(adapter);
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}

