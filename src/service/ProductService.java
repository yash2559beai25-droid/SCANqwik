package service;

import exceptions.InvalidInputException;
import exceptions.ProductNotFoundException;
import model.PriceComparator;
import model.Product;
import store.DataStore;
import utility.InputValidator;
import utility.StringTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Catalog in memory.
 * HashMap – ID lookup, HashSet – unique IDs, TreeMap – sorted by ID,
 * TreeSet – sorted categories, LinkedHashSet – recently scanned IDs,
 * ArrayList – lists for display.
 */
public class ProductService {

    private final Map<String, Product> catalog;
    private final Set<String> uniqueIds = new HashSet<String>();
    private final Set<String> categories = new TreeSet<String>();
    private final LinkedHashSet<String> recentlyScanned = new LinkedHashSet<String>();

    public ProductService() {
        this.catalog = DataStore.getInstance().getProductsById();
        rebuildIndexes();
    }

    public void rebuildIndexes() {
        uniqueIds.clear();
        categories.clear();
        Iterator<Product> iterator = catalog.values().iterator();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            uniqueIds.add(product.getProductId());
            categories.add(product.getCategory());
        }
    }

    public List<Product> getAllProducts() {
        rebuildIndexes();
        List<Product> list = new ArrayList<Product>(catalog.values());
        Collections.sort(list); // Comparable: name
        return list;
    }

    public List<Product> getAllSortedByPrice() {
        List<Product> list = getAllProducts();
        Collections.sort(list, new PriceComparator());
        return list;
    }

    public TreeMap<String, Product> getCatalogSortedById() {
        return new TreeMap<String, Product>(catalog);
    }

    public TreeSet<String> getCategories() {
        rebuildIndexes();
        return new TreeSet<String>(categories);
    }

    public LinkedHashSet<String> getRecentlyScanned() {
        return recentlyScanned;
    }

    public Product searchById(String productId) throws ProductNotFoundException {
        InputValidator.requireNonEmpty(productId, "Product ID");
        String key = StringTools.normalizeBarcode(productId);
        Product found = catalog.get(key);
        if (found == null) {
            // linear scan so intern()/equals path is visible if map key was interned
            for (Map.Entry<String, Product> entry : catalog.entrySet()) {
                if (StringTools.sameBarcode(key, entry.getKey())) {
                    found = entry.getValue();
                    break;
                }
            }
        }
        if (found == null) {
            throw new ProductNotFoundException("No product with ID " + productId.trim());
        }
        recentlyScanned.add(found.getProductId());
        return found;
    }

    public List<Product> searchByName(String name) throws ProductNotFoundException {
        InputValidator.requireNonEmpty(name, "Product name");
        List<Product> matches = new ArrayList<Product>();
        Iterator<Product> iterator = catalog.values().iterator();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            if (StringTools.nameContains(product.getName(), name)) {
                matches.add(product);
            }
        }
        if (matches.isEmpty()) {
            throw new ProductNotFoundException("No product name contains \"" + name.trim() + "\".");
        }
        return matches;
    }

    public List<Product> search(String query, int mode) throws ProductNotFoundException {
        if (mode == 1) {
            List<Product> one = new ArrayList<Product>();
            one.add(searchById(query));
            return one;
        }
        return searchByName(query);
    }

    public void addProduct(Product product) {
        InputValidator.requireNonEmpty(product.getProductId(), "Product ID");
        InputValidator.requireNonEmpty(product.getName(), "Name");
        InputValidator.requireNonEmpty(product.getCategory(), "Category");
        if (product.getPrice() <= 0) {
            throw new InvalidInputException("Price must be greater than zero.");
        }
        if (product.getStock() < 0) {
            throw new InvalidInputException("Stock cannot be negative.");
        }
        String id = StringTools.normalizeBarcode(product.getProductId());
        if (uniqueIds.contains(id) || catalog.containsKey(id)) {
            throw new InvalidInputException("Product ID already exists.");
        }
        product.setProductId(id);
        product.setName(StringTools.displayName(product.getName()));
        catalog.put(id, product);
        rebuildIndexes();
    }

    public void updateProduct(Product product) throws ProductNotFoundException {
        searchById(product.getProductId());
        catalog.put(product.getProductId(), product);
        rebuildIndexes();
    }

    public void deleteProduct(String productId) throws ProductNotFoundException {
        Product existing = searchById(productId);
        catalog.remove(existing.getProductId());
        recentlyScanned.remove(existing.getProductId());
        rebuildIndexes();
    }

    public HashMap<String, Product> asHashMap() {
        return new HashMap<String, Product>(catalog);
    }
}
