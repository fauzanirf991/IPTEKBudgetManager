<?php 

header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];
$id_aset      = $_POST['id_aset'];
$bukti = $_POST['bukti'];

if ( $key == "delete" ){

    $query = "DELETE FROM aset WHERE id_aset='$id_aset' ";

        if ( mysqli_query($conn, $query) ){

            $iparr =explode("/", $bukti);
            $picture_split = $iparr[5];

            if ( unlink("notaasetpict/".$picture_split) ){

                $result["value"] = "1";
                $result["message"] = "Success!";

                echo json_encode($result);
                mysqli_close($conn);

            } else {
            
                $response["value"] = "0";
                $response["message"] = "Error to delete a image! ".mysqli_error($conn);
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

} else {
    $response["value"] = "0";
    $response["message"] = "Error! ".mysqli_error($conn);
    echo json_encode($response);

    mysqli_close($conn);
}

?>