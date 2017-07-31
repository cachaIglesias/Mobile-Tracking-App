package ar.com.service.tracking.mobile.mobiletrackingservice.model;

/**
 * Created by miglesias on 08/07/17.
 */

public class Order {

    private String direccion;
    private String destinatario;
    private String producto;
    private Float valor;

    public Order(String dir, String dest, String prod, Float val){
        direccion = dir;
        destinatario = dest;
        producto = prod;
        valor = val;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }

    @Override
    public String toString(){
       return direccion + "\n" + destinatario + "\n" + producto + "         " + valor;
    }

//    // Constructor to convert JSON object into a Java class instance
//    public User(JSONObject object){
//        try {
//            this.name = object.getString("name");
//            this.hometown = object.getString("hometown");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Factory method to convert an array of JSON objects into a list of objects
//    // User.fromJson(jsonArray);
//    public static ArrayList<User> fromJson(JSONArray jsonObjects) {
//        ArrayList<User> users = new ArrayList<User>();
//        for (int i = 0; i < jsonObjects.length(); i++) {
//            try {
//                users.add(new User(jsonObjects.getJSONObject(i)));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return users;
//    }
}
