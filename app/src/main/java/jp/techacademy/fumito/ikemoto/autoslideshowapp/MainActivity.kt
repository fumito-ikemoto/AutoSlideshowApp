package jp.techacademy.fumito.ikemoto.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val imageUrlMutableList = mutableListOf<Uri>()
    //現在の画像が何番目かを表示（0始め）
    private var currentImageIndex = 0
    private var mTimer: Timer? = null
    private var mHandler = Handler()
    // タイマー用の時間のための変数
    private var mTimerSec = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        nextButton.setOnClickListener{
            updateImageView(currentImageIndex + 1)
        }

        backButton.setOnClickListener{
            updateImageView(currentImageIndex - 1)
        }

        playOrStopButton.setOnClickListener{
            playOrPauseSlide()
        }

        playOrStopButton.text = "再生"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    //アプリ内に収納されている画像データを取得し、最初の画像を表示する
    private fun getContentsInfo() {
        //リストのクリア
        imageUrlMutableList.clear()

        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUrlMutableList.add(imageUri)
            }while (cursor.moveToNext())

            val firstUri = imageUrlMutableList[0]
            imageView.setImageURI(firstUri)
            }
        cursor.close()
    }

    //引数のIndexを元に画像更新
    private fun updateImageView(index : Int) {

        if(imageUrlMutableList.count() <= 0)
        {
            return
        }

        var currentIndex : Int
        if(index >= imageUrlMutableList.count())
        {
            //最初に戻る
            currentIndex = 0
        }
        else if(index < 0)
        {
            //末尾のindexに移る
            currentIndex = imageUrlMutableList.count() - 1
        }
        else
        {
            currentIndex = index
        }

        var imageUri = imageUrlMutableList[currentIndex]
        imageView.setImageURI(imageUri)
        currentImageIndex = currentIndex
    }

    //状況によってスライドショーを再生or停止する
    private fun playOrPauseSlide()
    {
        if(imageUrlMutableList.count() <= 0) {
            return
        }

        if(mTimer == null)
        {
            //タイマー開始
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mTimerSec += 2
                    mHandler.post {
                        updateImageView(currentImageIndex + 1)
                    }
                }
            }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定
            playOrStopButton.text = "停止"

            nextButton.isEnabled = false
            backButton.isEnabled = false
        }
        else
        {
            //mTimerがnullじゃない状態でココに来る
            mTimer!!.cancel()
            mTimer = null
            playOrStopButton.text = "再生"
            nextButton.isEnabled = true
            backButton.isEnabled = true
        }
    }
}
