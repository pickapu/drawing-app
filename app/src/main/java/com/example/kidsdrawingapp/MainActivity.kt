package com.example.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import com.example.kidsdrawingapp.databinding.DialogBrushSizeBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private  var mImageButtonCurrentPaint:ImageButton?=null
    private lateinit  var binding:ActivityMainBinding
    private lateinit var dialogBinding:DialogBrushSizeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.drawingv.setSizeForBrush(20.toFloat())

        mImageButtonCurrentPaint=binding.colorSelctor[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_selected)
        )
        binding.ibBrush.setOnClickListener {


                showBrushSizeChooserdialog()
        }
        binding.ibGallery.setOnClickListener {
            if (isReadStorageAllowed()){

                val pickPhotoIntent=Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, Gallery)



            }else
            {
                requestStoragePermission()
            }
        }
        binding.ibBack.setOnClickListener {
            binding.drawingv.onClickUndo()
        }
        binding.ibSave.setOnClickListener {
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(binding.frame)).execute()
            }else{
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){

            if(requestCode== Gallery){
                try{
                    if(data!!.data!=null){
                        binding.ibackground.visibility=View.VISIBLE
                        binding.ibackground.setImageURI(data.data)
                    }else{
                        Toast.makeText(
                            this@MainActivity,"error parsing the image",Toast.LENGTH_LONG
                        ).show()
                    }

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }
    private fun showBrushSizeChooserdialog(){
        dialogBinding= DialogBrushSizeBinding.inflate(layoutInflater)
        val brushDialog= Dialog(this)
        brushDialog.setContentView(dialogBinding.root)
        brushDialog.setTitle("brush size:")
        val smallbtn=dialogBinding.imgSmallBush
        smallbtn.setOnClickListener{
            binding.drawingv.setSizeForBrush(10.toFloat())

            brushDialog.dismiss()

        }
        val mediumbtn=dialogBinding.imgMediumBush
        mediumbtn.setOnClickListener{
            binding.drawingv.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()

        }
        val largeBtn=dialogBinding.imglargeBush
        largeBtn.setOnClickListener{
            binding.drawingv.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()

    }
    fun paintClicked(view: View){
        if(view!==mImageButtonCurrentPaint){
            val imageButton=view as ImageButton
            val colorTag=imageButton.tag.toString()
            binding.drawingv.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_selected)

            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint=view

        }
    }
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"need permission",Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),

            STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,"permission granted",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@MainActivity,"denied",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun isReadStorageAllowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result==PackageManager.PERMISSION_GRANTED
    }
    private fun getBitmapFromView(view: View):Bitmap{
        val returnedBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(returnedBitmap)
        val bgDrawable=view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }
    private inner class BitmapAsyncTask(val mBimap:Bitmap):AsyncTask<Any,Void,String>(){
        private lateinit var mProgressBar:Dialog
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg p0: Any?): String {

var result=""
            if(mBimap!=null){
                try{
                    val bytes=ByteArrayOutputStream()
                    mBimap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f= File(externalCacheDir!!.absoluteFile.toString()
                            +File.separator+"kidsDrawingApp"
                            +System.currentTimeMillis()/1000+".png")
                    val fo=FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result=f.absolutePath
                }catch(e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
            return result



        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (!result!!.isEmpty()){
                Toast.makeText(this@MainActivity,
                "file saved",
                Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@MainActivity,
                "something wrong",
                Toast.LENGTH_LONG).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path, uri->val shareintent=Intent()
                shareintent.action=Intent.ACTION_SEND
                shareintent.putExtra(Intent.EXTRA_STREAM,uri)
                shareintent.type="image/png"
                startActivity(
                    Intent.createChooser(
                        shareintent,"share"
                    )
                )
            }
        }
        private fun showProgressDialog(){
        mProgressBar= Dialog(this@MainActivity)
        mProgressBar.setContentView(R.layout.dialog_custom_progress)
        mProgressBar.show()
        }
        private fun cancelProgressDialog(){
            mProgressBar.cancel()
        }

    }
    companion object{
        private const val STORAGE_PERMISSION_CODE=1
        private const val Gallery=2
    }
}