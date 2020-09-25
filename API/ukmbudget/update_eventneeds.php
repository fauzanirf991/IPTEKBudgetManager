<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];

if ( $key == "update" ){

    $id_pengeluaran       = $_POST['id_pengeluaran'];
    $nama_pengeluaran     = $_POST['nama_pengeluaran'];
    $jumlah_item          = $_POST['jumlah_item'];
    $harga_satuan         = $_POST['harga_satuan'];
    $tgl_pengeluaran      = $_POST['tgl_pengeluaran'];
    $bukti                = $_POST['bukti'];

    $jumlah_item = (int)$jumlah_item;
    $harga_satuan = (int)$harga_satuan;

    $ttlharga = $jumlah_item*$harga_satuan;
    $tgl_pengeluaran =  date('Y-m-d', strtotime($tgl_pengeluaran));

    $query = "UPDATE pengeluarankegiatan SET 
    nama_pengeluaran='$nama_pengeluaran',  
    jumlah_item='$jumlah_item',
    harga_satuan='$harga_satuan',
    tgl_pengeluaran='$tgl_pengeluaran',
    bukti='$bukti'
    WHERE id_pengeluaran='$id_pengeluaran' ";


    //update dana
    $dana = mysqli_query($conn, "SELECT jumlahdana FROM pagudana WHERE id_pagudana=1");
    while($row = mysqli_fetch_assoc($dana)) {
        $jumlahdana = $row['jumlahdana'];
    }

    $outcome = mysqli_query($conn, "SELECT total_harga FROM pengeluarankegiatan WHERE id_pengeluaran='$id_pengeluaran' ");
    while($row2 = mysqli_fetch_assoc($outcome)) {
        $ttloutcome = $row2['total_harga'];
    }
    $jumlahdana = (int)$jumlahdana;
    $ttloutcome = (int)$ttloutcome;

    $sisadana = $jumlahdana - ($ttlharga - $ttloutcome);
    $updana = mysqli_query($conn, "UPDATE pagudana SET jumlahdana = '$sisadana' WHERE id_pagudana=1");

        if ( mysqli_query($conn, $query) ){

            if ($bukti == null) {

                $result["value"] = "1";
                $result["message"] = "Success";
    
                echo json_encode($result);
                mysqli_close($conn);

            } else {

                $path = "notapict/$id_pengeluaran.jpeg";
                $finalPath = "/ukmbudget/".$path;

                $insert_picture = "UPDATE pengeluarankegiatan SET bukti='$finalPath' WHERE id_pengeluaran='$id_pengeluaran' ";
            
                if (mysqli_query($conn, $insert_picture) ) {
            
                    if ( file_put_contents( $path, base64_decode($bukti) ) ) {
                        
                        $result["value"] = "1";
                        $result["message"] = "Success!";
            
                        echo json_encode($result);
                        mysqli_close($conn);
            
                    } else {
                        
                        $response["value"] = "0";
                        $response["message"] = "Error! ".mysqli_error($conn);
                        echo json_encode($response);

                        mysqli_close($conn);
                    }

                }else{
                    $response["value"] = "0";
                    $response["message"] = "Error! ".mysqli_error($conn);
                    echo json_encode($response);

                    mysqli_close($conn);
                }
            }

        } 
        else {
            $response["value"] = "0";
            $response["message"] = "Error! ".mysqli_error($conn);
            echo json_encode($response);

            mysqli_close($conn);
        }
}

?>