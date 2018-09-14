package enesates.com.artbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    ImageView imageView;
    EditText editText;
    static SQLiteDatabase database; // static demek diğer yerlerden de erişilebilir demek yani MainActivityden de erişebiliriz.
    Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);


        Intent intent = (Intent) getIntent();

        String info = intent.getStringExtra("info");


        // Burada eğer yeni bir şey kaydedilecekse save button gözükecek eğer eski bir şeyse button gözükmeyecek.
        if(info.equalsIgnoreCase("new")) {

            Bitmap background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.background);
            imageView.setImageBitmap(background);// Burada da yeni bir şey kaydedilirken görüntü default yapılıyor.
            button.setVisibility(View.VISIBLE);
            editText.setText("");//Burada kullanıcı geldiğinde boş gözüküp kolaylık sağlaması amacı güder.

        }
        else{
            String name = intent.getStringExtra("name");
            editText.setText(name);
            int position = intent.getIntExtra("position", 0);
            imageView.setImageBitmap(MainActivity.artImage.get(position));
            button.setVisibility(View.INVISIBLE);
        }

    }

    public void select(View view){

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Burada izin verilmediyse  izin istiyoruz

            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        }
        else{

            // İzin verildiyse kullanıcının albümünü açıp aramaya başlaya biliriz.

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);
            // startActivityForResult(intent,1); un  anlamı, bu activitem bir sonuç(result) için yapılıyor demek
            // //yani MediaStore ordan Images ordan Mediaya git al. Bilgi : requestcode burada 1 verdik

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // Kullanıcı bize ilk izin verdiğinde ne yapıcaz onu yazıyoruz
        // Hatırlatma requestCode select() metodunda 2 olarak verdik.

        if(requestCode == 2) {

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
                // startActivityForResult(intent,1); un  anlamı, bu activitem bir sonuç(result) için yapılıyor demek
                // //yani MediaStore ordan Images ordan Mediaya git al. Bilgi : requestcode burada 1 verdik

            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //requestCode select(view) metodundan geliyor.
        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {

            // Eğer bu if'in içine giriyorsa kullanıcı bir resim seçmiş demektir.

            Uri images = data.getData();

            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),images);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view){

        // Burada database'e kaydedeceğimiz 2 şey var. 1.si stringimiz, 2.si image'ımız.

        String artName = editText.getText().toString();

        //Aşağıda ise bir image'ı alıcaz ama alırken compress ederek Bitmap olarak değilde bitlere, bytelara çevirerek alıcaz.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream); // Burada zipleme yaptık ve quality 0-100 arasında değer alabilir.
        byte[] byteArray = outputStream.toByteArray();

        try {

            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, image BLOB)");
            // Eğer kaydettiğimiz şey VARCAHR, INT değilse yani dataysa BLOB diyerek kaydediyoruz.

            // database eimage kaydederken aşağıdaki gibi yaparız.
            String sqlString = "INSERT INTO arts(name, image) VALUES(?, ?)";
            SQLiteStatement statement = database.compileStatement(sqlString);
            statement.bindString(1, artName); // İlk index 1. ? yani artname
            statement.bindBlob(2, byteArray); // İkinci index 2. ? yani o da byteArray i temsil ediyor.
            statement.execute();

        }catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);// Böylece kaydetme işleminden sonra MainActivitye giderek ekranını gösteriyoruz.

    }

}
