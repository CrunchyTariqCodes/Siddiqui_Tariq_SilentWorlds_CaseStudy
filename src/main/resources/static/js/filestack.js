
console.log("hello");
const client = filestack.init(filestackAPIKey);

//pass this object as an argument in the picker method.
const options = {
    onFileUploadFinished: callback =>{
        const imgURL = callback.url;
        $('#avatar').val(imgURL);
        $('#avatarPreview').attr('src',imgURL);
    }
}
//event listener for listening to a click on a button
$('#addPicture').click(function (event){
    event.preventDefault();

    //tells filestack to open their file picker interface.
    // the picker method can take an argument of an options object
    // user then specifies what they want the picker to do
    client.picker(options).open();
})