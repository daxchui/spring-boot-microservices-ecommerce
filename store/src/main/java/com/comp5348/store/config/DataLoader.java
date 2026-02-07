package com.comp5348.store.config;

import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public DataLoader(ProductRepository productRepository,
                      WarehouseRepository warehouseRepository,
                      WarehouseStockRepository warehouseStockRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    @Override
    public void run(String... args) {

        // Create products
        if (productRepository.count() == 0) {
            Product laptop = new Product();
            laptop.setName("Laptop");
            laptop.setPrice(1200.00);
            productRepository.save(laptop);

            Product phone = new Product();
            phone.setName("Phone");
            phone.setPrice(800.00);
            productRepository.save(phone);

            Product tv = new Product();
            tv.setName("TV");
            tv.setPrice(1500.00);
            productRepository.save(tv);
        }

        // Create warehouses
        if (warehouseRepository.count() == 0) {
            Warehouse sydney = new Warehouse();
            sydney.setName("Sydney Warehouse");
            sydney.setLocation("Sydney");
            warehouseRepository.save(sydney);

            Warehouse melbourne = new Warehouse();
            melbourne.setName("Melbourne Warehouse");
            melbourne.setLocation("Melbourne");
            warehouseRepository.save(melbourne);

            Warehouse brisbane = new Warehouse();
            brisbane.setName("Brisbane Warehouse");
            brisbane.setLocation("Brisbane");
            warehouseRepository.save(brisbane);

            Product laptop = productRepository.findByName("Laptop").get();
            Product phone = productRepository.findByName("Phone").get();
            Product tv = productRepository.findByName("TV").get();

            // Sydney stocks
            WarehouseStock s1 = new WarehouseStock();
            s1.setWarehouse(sydney);
            s1.setProduct(laptop);
            s1.setQuantity(5);
            warehouseStockRepository.save(s1);

            WarehouseStock s2 = new WarehouseStock();
            s2.setWarehouse(sydney);
            s2.setProduct(phone);
            s2.setQuantity(3);
            warehouseStockRepository.save(s2);

            WarehouseStock s3 = new WarehouseStock();
            s3.setWarehouse(sydney);
            s3.setProduct(tv);
            s3.setQuantity(2);
            warehouseStockRepository.save(s3);

            // Melbourne stocks
            WarehouseStock m1 = new WarehouseStock();
            m1.setWarehouse(melbourne);
            m1.setProduct(laptop);
            m1.setQuantity(8);
            warehouseStockRepository.save(m1);

            WarehouseStock m2 = new WarehouseStock();
            m2.setWarehouse(melbourne);
            m2.setProduct(phone);
            m2.setQuantity(10);
            warehouseStockRepository.save(m2);

            WarehouseStock m3 = new WarehouseStock();
            m3.setWarehouse(melbourne);
            m3.setProduct(tv);
            m3.setQuantity(5);
            warehouseStockRepository.save(m3);

            // Brisbane stocks
            WarehouseStock b1 = new WarehouseStock();
            b1.setWarehouse(brisbane);
            b1.setProduct(laptop);
            b1.setQuantity(4);
            warehouseStockRepository.save(b1);

            WarehouseStock b2 = new WarehouseStock();
            b2.setWarehouse(brisbane);
            b2.setProduct(phone);
            b2.setQuantity(6);
            warehouseStockRepository.save(b2);

            WarehouseStock b3 = new WarehouseStock();
            b3.setWarehouse(brisbane);
            b3.setProduct(tv);
            b3.setQuantity(1);
            warehouseStockRepository.save(b3);
        }

        System.out.println("Sample data loaded!");
    }
}
