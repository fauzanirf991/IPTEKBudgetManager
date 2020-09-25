<?php
require_once 'connect.php';

    $key = $_POST['key'];
    $jumlahdana     = $_POST['jumlah_dana'];
    $tgl_diterima      = $_POST['tgl_diterima'];
    $bukti    = $_POST['bukti'];




if ( $key == "insert" ){
    $check_row_dana = mysqli_query($conn, "SELECT * FROM pagudana");

    if(mysqli_num_rows($check_row_dana) < 1){

        $jumlahdana = (int)$jumlahdana;
        $tgl_diterima = date('Y-m-d', strtotime($tgl_diterima));

        $query = "INSERT INTO pagudana (jumlahdana, tgl_diterima, bukti)
        VALUES ('$jumlahdana', '$tgl_diterima', '$bukti') ";
        $query2 = mysqli_query($conn, "INSERT INTO pagudanastatic (jumlahdana, id_pagudana)
        VALUES ('$jumlahdana',1) ");


            if ( mysqli_query($conn, $query) ){

            
                $id = mysqli_insert_id($conn);
                $path = "buktipagudanapict/$id.jpeg";
                $finalPath = "/ukmbudget/".$path;

                $insert_picture = "UPDATE pagudana SET bukti='$finalPath' WHERE id_pagudana='$id' ";
            
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
    } else{
        $response["value"] = "0";
        $response["message"] = "Tidak bisa Menambah lebih dari 1 data ! ".mysqli_error($conn);
        echo json_encode($response);

        mysqli_close($conn);
    }

    
}

?>