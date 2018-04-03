package com.example.camandgal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private  final int CAMERA_REQUEST= 1888; //Cualquier Num
	private static final int SELECT_PICTURE = 1;
	private String  selectedImagePath;
	Uri mCapturedImageURI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case R.id.media:
			openAddPhoto();
			return true;
					
        default:
            return super.onOptionsItemSelected(item);
		}

	}
	
	
	/**********************************************************************
	 * 	ABRE EL DIALOGO PARA SELECCIÓN DE CÁMARA O GALERÍA MEDIANTE INTENT
	 * ********************************************************************/
	public void openAddPhoto() {

        String[] items=new String[]{ getString(R.string.camara) , getString(R.string.galeria) };
        
        final Integer[] icons = new Integer[] {R.drawable.ic_camera, R.drawable.ic_gallery};
        
        ListAdapter adapter = new ArrayAdapterWithIcon(this, items, icons);
        
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.selecciona));
        dialog.setAdapter(adapter, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if(id==0){
                	
                	//CAMARA
                	String captured_image = System.currentTimeMillis() + ".jpg"; 
                    ContentValues values = new ContentValues();  
                    values.put(MediaStore.Images.Media.TITLE, captured_image);  
                    mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  
                    
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI); 
                    startActivityForResult(intent, CAMERA_REQUEST);

                }
                
                if(id==1){
                	
                	//	GALERÍA
                	Intent intent_galery = new Intent();
    	        	intent_galery.setType("image/*");
    	        	intent_galery.setAction(Intent.ACTION_GET_CONTENT);
    	        	intent_galery.addCategory(Intent.CATEGORY_OPENABLE);
    	            startActivityForResult(intent_galery, SELECT_PICTURE);
                    

                }
            }
        });     

        
        dialog.setNeutralButton("Cancelar",new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();               
            }});
        dialog.show();
    }
	
	/*******************************************************************
	 * 	CAPTURA LOS VALUES CUANDO EL INTENT SE CIERRAY REGRESA EL CONTROL
	 * *****************************************************************/
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			
			//DEFINIMOS DONDE APARECERÁ LA FOTO SELECCIONADA O TOMADA
			LinearLayout rl = (LinearLayout) findViewById(R.id.lylImage);
			rl.removeAllViewsInLayout();
			
			final ImageView ViewPic = new ImageView(this);
			ViewPic.setImageResource(0);
			ViewPic.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));
			rl.addView(ViewPic);
			
			if (requestCode == CAMERA_REQUEST){
				
				selectedImagePath = getPath(mCapturedImageURI);
				
			}else if (requestCode == SELECT_PICTURE){
				
				Uri selectedImageUri = data.getData();
				selectedImagePath = getPath(selectedImageUri);
				
			}
			
			ViewPic.setImageBitmap(Loadimage(selectedImagePath.toString(), ViewPic));
			
		}
		
	
	}

	/*******************************************************************
	 * 	ADAPTER DE LISTA DE ICONOS PARA EL DIALOG
	 * *****************************************************************/
	public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

		private List<Integer> images;

		public ArrayAdapterWithIcon(Context context, List<String> items, List<Integer> images) {
		    super(context, android.R.layout.select_dialog_item, items);
		    this.images = images;
		}

		public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
		    super(context, android.R.layout.select_dialog_item, items);
		    this.images = Arrays.asList(images);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    View view = super.getView(position, convertView, parent);
		    TextView textView = (TextView) view.findViewById(android.R.id.text1);
		    textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
		    textView.setCompoundDrawablePadding(
		            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
		    return view;
		}

	}
	
	/**/
	public String getPath(Uri uri) {

		String[] projection = { MediaStore.Images.Media.DATA };
		//Cursor cursor = managedQuery(uri, projection, null, null, null);
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	public static class Util {
		
	    public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
	        int scaleHeight = (int) (bm.getHeight() * scalingFactor);
	        int scaleWidth = (int) (bm.getWidth() * scalingFactor);

	        return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
	    }

	}
	
	private float getBitmapScalingFactor(Bitmap bm, ImageView ImgView) {
        
    	DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
    	int displayWidth = metrics.widthPixels;
        
        // Get margin to use it for calculating to max width of the ImageView
        LinearLayout.LayoutParams layoutParams = 
            (LinearLayout.LayoutParams)ImgView.getLayoutParams();
        
        int leftMargin = layoutParams.leftMargin;
        int rightMargin = layoutParams.rightMargin;
        // Calculate the max width of the imageView
        int imageViewWidth = displayWidth - (leftMargin + rightMargin);
        // Calculate scaling factor and return it
        return ( (float) imageViewWidth / (float) bm.getWidth() );
    }
	
	public Bitmap Loadimage(String Path, ImageView img){
    	File f=new File(Path);
    	
    	try {
	    	FileInputStream fileis=new FileInputStream(f);
	    	BufferedInputStream bufferedstream=new BufferedInputStream(fileis);
	    	BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Config.RGB_565;
			options.inSampleSize = 2;
			Bitmap bMap = BitmapFactory.decodeStream(bufferedstream, null, options);
			float scalingFactor = getBitmapScalingFactor(bMap, img);
			Bitmap newBitmap = Util.ScaleBitmap(bMap, scalingFactor);
			
			return newBitmap;
			
	    	} catch (IOException e) {                   
				e.printStackTrace();
			}
    	
			return null;
    	
    }
}
