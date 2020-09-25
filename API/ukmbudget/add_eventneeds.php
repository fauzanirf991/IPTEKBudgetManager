<?php
require_once 'connect.php';

$key = $_POST['key'];

$nama_pengeluaran     = $_POST['nama_pengeluaran'];
$jumlah_item      = $_POST['jumlah_item'];
$harga_satuan    = $_POST['harga_satuan'];
$tgl_pengeluaran = $_POST['tgl_pengeluaran'];
$bukti = $_POST['bukti'];
$id_kegiatan = $_POST['id_kegiatan'];

if ( $key == "insert" ){
    if($id_kegiatan == 0){
        $response["value"] = "0";
        $response["message"] = "Error! ".mysqli_error($conn);
        echo json_encode($response);

        mysqli_close($conn);
    }else{
        $Jumlah_item = (int)$jumlah_item;
        $harga_satuan = (int)$harga_satuan;
        $tgl_pengeluaran = date('Y-m-d', strtotime($tgl_pengeluaran));

        $query = "INSERT INTO pengeluarankegiatan (nama_pengeluaran, jumlah_item, harga_satuan, tgl_pengeluaran, bukti, id_kegiatan)
        VALUES ('$nama_pengeluaran', '$jumlah_item', '$harga_satuan', '$tgl_pengeluaran', '$bukti', '$id_kegiatan') ";

            if ( mysqli_query($conn, $query) ){
                $id = mysqli_insert_id($conn);
                $path = "notapict/$id.jpeg";
                $finalPath = "/ukmbudget/".$path;

                $insert_picture = "UPDATE pengeluarankegiatan SET bukti='$finalPath' WHERE id_pengeluaran='$id' ";

                //update pagudana
                $dana = mysqli_query($conn, "SELECT jumlahdana FROM pagudana WHERE id_pagudana=1");
                while($row = mysqli_fetch_assoc($dana)) {
                    $jumlahdana = $row['jumlahdana'];
                }

                $outcome = mysqli_query($conn, "SELECT total_harga FROM pengeluarankegiatan WHERE id_pengeluaran='$id' ");
                while($row2 = mysqli_fetch_assoc($outcome)) {
                    $ttloutcome = $row2['total_harga'];
                }
                $jumlahdana = (int)$jumlahdana;
                $ttloutcome = (int)$ttloutcome;

                $sisadana = $jumlahdana - $ttloutcome;
                $updana = mysqli_query($conn,"UPDATE pagudana SET jumlahdana = '$sisadana' WHERE id_pagudana=1");

                
                    if (mysqli_query($conn, $insert_picture)) {
                
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

                    }else {
                            
                        $response["value"] = "0";
                        $response["message"] = "Error! ".mysqli_error($conn);
                        echo json_encode($response);

                        mysqli_close($conn);
                    }
                

            } 
            else {
                $response["value"] = "0";
                $response["message"] = "Error! ".mysqli_error($conn);
                echo json_encode($response);

                mysqli_close($conn);
            }
        }
}

?>