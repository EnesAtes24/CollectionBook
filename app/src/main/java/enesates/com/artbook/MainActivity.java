package enesates.com.artbook;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<Bitmap> artImage;
    // static Bitmap chosenImage; Aşağıda image aktarımında 2. yöntem yapılacaksa bu tanımlama yapılmalı.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // menu ile MainActivityi bağlamamız için var olan onCreateOptionsMenu'yü override ettik.

        // MenuInflater menu'yü kullanmamız için gerekli obje
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art, menu); // Menüyü çıkar anlamına geliyor. Böylece hangi menu'yü çıkaracağımızı MainActivity'mize söylemiş olduk

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Burada da ben menu'yü seçersem ne olacak onu yazıyoruz.
        // Yani doğal olarak burada da başka bir Activity oluşturucaz ve Intent yapıcaz.


        // Tabi öncelikle Intenti başlatmadan önce kontrol yapabiliriz çünkü başka birşeyde seçilmiş olabilir.
        if(item.getItemId() == R.id.add_art){

            Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
            intent.putExtra("info", "new");
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);
        // ListViewler ArrayListlerle kullanılır (genellikle diyelim:D)
        final ArrayList<String> artName = new ArrayList<String>();
        artImage = new ArrayList<Bitmap>();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, artName);
        listView.setAdapter(arrayAdapter);

        try {

            Main2Activity.database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            Main2Activity.database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, image BLOB)");

            Cursor cursor = Main2Activity.database.rawQuery("SELECT * FROM arts", null);

            int nameIx = cursor.getColumnIndex("name");
            int imageIx = cursor.getColumnIndex("image");

            cursor.moveToFirst(); //Başa götür indexi.

            while(cursor != null) {

                artName.add(cursor.getString(nameIx)); // Burada isimleri arraye ekledik

                // Aşağıda image eklemek için dönüştürme yapacağız sonra artImage'e ekliycez.
                byte[] byteArray = cursor.getBlob(imageIx);
                Bitmap image = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
                artImage.add(image);

                cursor.moveToNext();// Burada bir loop olduğu için bir sonrakine geç diyoruz.

                //Aşağıdaki kod eğer bir datayı değiştirdiysek arrayAdapter'a haber veriyor. O da listview içinde hemen güncelleyip hemen yeni datayı kullanıcıya gösteriyor.
                arrayAdapter.notifyDataSetChanged();

            }

        } catch( Exception e ) {
            e.printStackTrace();
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                intent.putExtra("info", "old");
                intent.putExtra("name", artName.get(position));
                intent.putExtra("position", position);
                startActivity(intent);
                // chosenImage = artImage.get(position); => (2. yöntem olarak kullanılabilir) Tıklandığı yerdeki ilgili yere gelen array içindeki image'ı alıyor chosenImage'a sabitliyor.

            }
        });

    }
}
