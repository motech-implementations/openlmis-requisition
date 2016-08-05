package org.openlmis.referencedata.repository;

import org.junit.After;
import org.junit.Before;
import org.openlmis.product.domain.Product;
import org.openlmis.product.domain.ProductCategory;
import org.openlmis.product.repository.ProductCategoryRepository;
import org.openlmis.product.repository.ProductRepository;
import org.openlmis.referencedata.domain.Stock;
import org.springframework.beans.factory.annotation.Autowired;

public class StockRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Stock> {

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  ProductCategoryRepository productCategoryRepository;

  private Product product = new Product();

  StockRepository getRepository() {
    return this.stockRepository;
  }

  @Before
  public void setUp() {
    productCategoryRepository.deleteAll();
    ProductCategory productCategory1 = new ProductCategory();
    productCategory1.setCode("PC1");
    productCategory1.setName("PC1 name");
    productCategory1.setDisplayOrder(1);
    productCategoryRepository.save(productCategory1);

    productRepository.deleteAll();
    product.setProductCategory(productCategory1);
    product.setPrimaryName("productName");
    product.setCode("productCode");
    product.setDispensingUnit("unit");
    product.setDosesPerDispensingUnit(10);
    product.setPackSize(1);
    product.setPackRoundingThreshold(0);
    product.setRoundToZero(false);
    product.setActive(true);
    product.setFullSupply(true);
    product.setTracer(false);
    productRepository.save(product);
  }

  Stock generateInstance() {
    Stock stock = new Stock();
    stock.setProduct(product);
    stock.setStoredQuantity(1234L);
    return stock;
  }

  @After
  public void cleanUp() {
    stockRepository.deleteAll();
    productRepository.deleteAll();
  }

}