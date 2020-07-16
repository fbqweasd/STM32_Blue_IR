package com.example.bluetooth

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var bluetoothAdapter:BluetoothAdapter ?= null
    var devices:Set<BluetoothDevice> ?= null
    var pariedDeviceCount:Int ?= null
    var bluetoothDevice:BluetoothDevice ?= null
    var bluetoothSocket: BluetoothSocket ?= null
    var outputStream : OutputStream ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BT_connet.setOnClickListener {
            if(bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
                // 여기에 처리 할 코드를 작성하세요.
                Toast.makeText(this@MainActivity, "블루투스를 지원하지 않는 단말 입니다.", Toast.LENGTH_SHORT).show()
            }
            else { 
                if (bluetoothAdapter!!.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                    runOnUiThread {
                        selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                    }

                } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                    Toast.makeText(this@MainActivity, "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, 1)
                }
            }
        }

        button.setOnClickListener {
            sendData('A'.toString())
        }

        button2.setOnClickListener {
            sendData('B'.toString())
        }

    }

    fun selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter!!.getBondedDevices()

        // 페어링 된 디바이스의 크기를 저장
        pariedDeviceCount = (devices as MutableSet<BluetoothDevice>?)?.size
        // 페어링 되어있는 장치가 없는 경우
        if (pariedDeviceCount === 0) {
            // 페어링을 하기위한 함수 호출
        } else { // 페어링 되어있는 장치가 있는 경우
            // 디바이스를 선택하기 위한 다이얼로그 생성
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록")
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            val list: MutableList<String> = ArrayList()
            // 모든 디바이스의 이름을 리스트에 추가
            for (bluetoothDevice in (this.devices as MutableSet<BluetoothDevice>?)!!) {
                list.add(bluetoothDevice.name)
            }
            list.add("취소")

            // List를 CharSequence 배열로 변경
            val charSequences =
                list.toTypedArray<CharSequence>()
            list.toTypedArray<CharSequence>()

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences,
                DialogInterface.OnClickListener { dialog, which -> // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString())
                })

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false)

            // 다이얼로그 생성
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

    fun connectDevice(deviceName:String){
        var tempDevice:BluetoothDevice

        for(tempDevice in this!!.devices!!) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료

            if(deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }

        var uuid : UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        try {
            Log.d("Debug", bluetoothDevice?.address)

            bluetoothAdapter?.cancelDiscovery()
            bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()

            if(bluetoothSocket == null){
                Toast.makeText(this@MainActivity, "블루투스 연결 실패", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this@MainActivity, "블루투스 연결 완료", Toast.LENGTH_SHORT).show()
            }
            // 데이터 송,수신 스트림을 얻어옵니다.
            outputStream = bluetoothSocket?.outputStream;
            //inputStream = bluetoothSocket.getInputStream();
            // 데이터 수신 함수 호출
            //receiveData();
        } catch (e : IOException) {
            Toast.makeText(this@MainActivity, "블루투스 연결 IO Error", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun sendData(text : String){
        var text_string = text

        text_string += "\n";
        try{
            // 데이터 송신
            outputStream?.write(text_string.toByteArray(), 0, text_string.length)

        }catch(e : Exception ) {
            e.printStackTrace()
        }
    }
}
