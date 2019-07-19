import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * This User class only has the username field in this example.
 * <p>
 * However, in the real project, this User class can contain many more things,
 * for example, the user's shopping cart items.
 */
public class User {

    private final String username;
    //private final String userId;
    private List<String> shoppingCart = new ArrayList<String>();
    private List<Integer> quantities = new ArrayList<Integer>();

    public User(String username) {
        this.username = username;
        //this.userId = username;
    }

    public String getUsername() {
        return this.username;
    }
    
    public List<String> getCart() {
        return this.shoppingCart;
    }
    
    public void addToCart(String item) {
        shoppingCart.add(item);
    }
    
    public void delFromCart(String item) {
    	shoppingCart.remove(item);
    }
    
    public int posCart(String item) {
    	return shoppingCart.indexOf(item);
    }
	
    public void resetCart() {
    	shoppingCart.clear();
    }
    
    public void sortCart() {
    	Collections.sort(shoppingCart);
    }
    
    public List<Integer> getQuantities() {
        return this.quantities;
    }
    
    public void addToQuantities(int ind, Integer num) {
    	quantities.add(ind, num);
    }
    
    public void delFromQuantities(int index) {
    	quantities.remove(index);
    }
    /*
    public void posQuantities(Integer item) {
    	quantities.indexOf(item);
    }
    */
    public void updateQuantities(int index, int newValue) {
    	quantities.set(index, newValue);
    }
}
