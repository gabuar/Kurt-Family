<?php
$host = "localhost";
$user = "root"; // default XAMPP user
$password = ""; // default XAMPP password (empty by default)
$database = "your_database_name"; // replace with your actual DB name

$conn = new mysqli($host, $user, $password, $database);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>
