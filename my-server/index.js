const express = require('express');
 const app = express();
 const port = 3002;
 const morgan = require("morgan");
 const bodyParser = require("body-parser");
 const cors = require("cors");
 const cookieParser = require("cookie-parser");
 const session = require('express-session');
 const { MongoClient, ObjectId } = require('mongodb');
 const fileUpload = require("express-fileupload");
 const path = require("path");
 const fs = require("fs");
 
 // Middleware setup
 app.use(morgan("combined"));
 app.use(bodyParser.json({ limit: '10mb' }));
 app.use(bodyParser.urlencoded({ extended: true, limit: '10mb' }));
 app.use(cors({
   origin: 'http://localhost:4200',
   methods: ['GET', 'POST', 'PUT', 'DELETE'],
   allowedHeaders: ['Content-Type', 'Authorization'],
   credentials: true 
 }));
 app.use(cookieParser());
 app.use(session({
   secret: "Shh, its a secret!",
   resave: false,
   saveUninitialized: true,
   cookie: { maxAge: 2 * 24 * 60 * 60 * 1000 } // Session timeout: 2 days
 }));
 app.use(fileUpload());
 
 // Static file serving
 app.use(
   "/images",
   cors(),
   express.static(path.join(__dirname, "public", "images"))
 );
 
 // Tạo thư mục upload nếu chưa tồn tại
 const uploadDir = path.join(__dirname, "upload");
 if (!fs.existsSync(uploadDir)) {
   fs.mkdirSync(uploadDir);
 }
 
 // MongoDB connection
 const client = new MongoClient("mongodb://127.0.0.1:27017");
 client.connect();
 const database = client.db("EvoCasa");
 const productCollection = database.collection("Product");
 const categoryCollection = database.collection("Category");
 const customerCollection = database.collection("Customer");
 const orderCollection = database.collection("Order");
 const accountCollection = database.collection("Account");
 const adminCollection = database.collection("Admin");
 
 const decodeHtmlEntities = (text) => {
   return text
     .replace(/&nbsp;/g, " ")
     .replace(/&amp;/g, "&")
     .replace(/&quot;/g, '"')
     .replace(/&apos;/g, "'")
     .replace(/&lt;/g, "<")
     .replace(/&gt;/g, ">");
 };
 
 const stripHtml = (html) => {
   if (!html) return "";
   const textWithoutHtml = html.replace(/<[^>]*>?/gm, ""); 
   return decodeHtmlEntities(textWithoutHtml); 
 };
 
 // Middleware to initialize cart in session
 app.use((req, res, next) => {
   if (!req.session.cart) {
     req.session.cart = []; 
   }
   next();
 });
 
 // Server startup
 app.listen(port, () => {
   console.log(`Server running on http://localhost:${port}`);
 });
 
 // Default route
 app.get("/", (req, res) => {
   res.send("Welcome to the EvoCasa API!");
 });
 
 // ===== PRODUCT APIS =====
 
 // API to get all products
 app.get("/products", async (req, res) => {
   try {
     const result = await productCollection.find({}).toArray();
     res.status(200).send(result);
   } catch (error) {
     console.error("Error fetching products:", error);
     res.status(500).send({ error: "Error fetching products" });
   }
 });

// API to get products by category
app.get("/products/category/:categoryId", async (req, res) => {
  try {
    const { categoryId } = req.params;
    const result = await productCollection.find({ category_id: categoryId }).toArray();
    res.status(200).send(result);
  } catch (error) {
    console.error("Error fetching products by category:", error);
    res.status(500).send({ error: "Error fetching products by category" });
  }
});

// API to get a specific product
app.get("/products/:identifier", async (req, res) => {
  try {
    const { identifier } = req.params;

    let query;
    if (ObjectId.isValid(identifier)) {
      query = { _id: new ObjectId(identifier) };
    } else {
      query = { Name: decodeURIComponent(identifier) };
    }

    const result = await productCollection.findOne(query);
    if (!result) {
      return res.status(404).send({ error: "Product not found" });
    }

    res.status(200).send(result);
  } catch (error) {
    console.error("Error fetching product:", error);
    res.status(500).send({ error: "Error fetching product" });
  }
});

// API to create a new product
app.post("/products", async (req, res) => {
  try {
    const {
      category_id,
      Name,
      Price,
      Image,
      Description,
      Origin,
      Uses,
      Store,
      Quantity,
      Dimension,
      Story,
      ProductCare,
      ShippingReturn
    } = req.body;

    const newProduct = {
      category_id,
      Name,
      Price: parseInt(Price),
      Image: Array.isArray(Image) ? Image : [Image],
      Description: stripHtml(Description),
      Origin,
      Uses,
      Store,
      Quantity: parseInt(Quantity),
      Create_date: new Date(),
      Dimension,
      Story: stripHtml(Story),
      ProductCare: stripHtml(ProductCare),
      ShippingReturn: stripHtml(ShippingReturn)
    };

    const result = await productCollection.insertOne(newProduct);
    res.status(201).send({
      _id: result.insertedId,
      ...newProduct
    });
  } catch (error) {
    console.error("Error creating product:", error);
    res.status(500).send({ error: "Error creating product" });
  }
});

// API to update a product
app.put("/products/:id", async (req, res) => {
  try {
    const { id } = req.params;
    const {
      category_id,
      Name,
      Price,
      Image,
      Description,
      Origin,
      Uses,
      Store,
      Quantity,
      Dimension,
      Story,
      ProductCare,
      ShippingReturn
    } = req.body;

    const updatedProduct = {
      category_id,
      Name,
      Price: parseInt(Price),
      Image: Array.isArray(Image) ? Image : [Image],
      Description: stripHtml(Description),
      Origin,
      Uses,
      Store,
      Quantity: parseInt(Quantity),
      Dimension,
      Story: stripHtml(Story),
      ProductCare: stripHtml(ProductCare),
      ShippingReturn: stripHtml(ShippingReturn)
    };

    const result = await productCollection.updateOne(
      { _id: new ObjectId(id) },
      { $set: updatedProduct }
    );

    if (result.matchedCount === 0) {
      return res.status(404).send({ error: "Product not found" });
    }

    res.status(200).send({
      _id: id,
      ...updatedProduct
    });
  } catch (error) {
    console.error("Error updating product:", error);
    res.status(500).send({ error: "Error updating product" });
  }
});

// API to delete a product
app.delete("/products/:id", async (req, res) => {
  try {
    const { id } = req.params;
    const result = await productCollection.deleteOne({ _id: new ObjectId(id) });

    if (result.deletedCount === 0) {
      return res.status(404).send({ error: "Product not found" });
    }

    res.status(200).send({ message: "Product deleted successfully" });
  } catch (error) {
    console.error("Error deleting product:", error);
    res.status(500).send({ error: "Error deleting product" });
  }
});

// ===== CART APIS =====

// API to add a product to the cart
app.post("/cart", async (req, res) => {
  const { productId, quantity } = req.body;

  try {
    const product = await productCollection.findOne({ _id: new ObjectId(productId) });
    if (!product) {
      return res.status(404).send({ error: "Product not found" });
    }

    // Check if the product already exists in the cart
    const existingProduct = req.session.cart.find(item => item.productId === productId);
    if (existingProduct) {
      existingProduct.cartQuantity += quantity; 
    } else {
      req.session.cart.push({
        productId,
        _id: product._id,
        category_id: product.category_id,
        Name: product.Name,
        Price: product.Price,
        Image: product.Image,
        Description: product.Description,
        Origin: product.Origin,
        Uses: product.Uses,
        Store: product.Store,
        Quantity: product.Quantity,
        cartQuantity: quantity,
        Create_date: product.Create_date,
        Dimension: product.Dimension,
        Story: product.Story,
        ProductCare: product.ProductCare,
        ShippingReturn: product.ShippingReturn
      });
    }

    console.log("Cart updated:", req.session.cart);
    res.status(200).send(req.session.cart);
  } catch (error) {
    console.error("Error adding to cart:", error);
    res.status(400).send({ error: "Invalid product ID or request" });
  }
});

// API to get all products in the cart
app.get("/cart", (req, res) => {
  console.log("Fetching cart:", req.session.cart);
  res.status(200).send(req.session.cart);
});

// API to update product quantity in the cart
app.put("/cart", (req, res) => {
  const { productId, quantity } = req.body;

  const product = req.session.cart.find(item => item.productId === productId);
  if (!product) {
    return res.status(404).send({ error: "Product not found in cart" });
  }

  product.cartQuantity = quantity;
  console.log("Cart updated:", req.session.cart);
  res.status(200).send(req.session.cart);
});

// API to delete a product from the cart
app.delete("/cart/:productId", (req, res) => {
  const { productId } = req.params;

  req.session.cart = req.session.cart.filter(item => item.productId !== productId);
  console.log("Cart after deletion:", req.session.cart);
  res.status(200).send(req.session.cart);
});

// API to clear the entire cart
app.delete("/cart", (req, res) => {
  req.session.cart = [];
  console.log("Cart cleared");
  res.status(200).send(req.session.cart);
});
// ===== IMAGE UPLOAD APIS =====

// Get image
app.get("/image/:id", cors(), (req, res) => {
  const id = req.params["id"];
  res.sendFile(path.join(uploadDir, id));
});

// Upload image
app.post("/upload", (req, res) => {
  const { image } = req.files;
  if (!image) return res.status(400).send({ error: "No image file provided" });

  image.mv(path.join(uploadDir, image.name));
  res.status(200).send({
    message: "Image uploaded successfully",
    filename: image.name
  });
});

// ===== CATEGORY APIS =====
// Lấy thông tin Category
app.get("/categories", cors(), async (req, res) => {
  const result = await categoryCollection.find({}).toArray();
  res.send(result);
})

// Get category by ID
app.get("/categories/:id", cors(), async (req, res) => {
  var o_id = new ObjectId(req.params["id"]);
  const result = await categoryCollection.find({ _id: o_id }).toArray();
  res.send(result[0]);
});


app.put("/categories", cors(), async (req, res) => {
  await categoryCollection.updateOne(
    { _id: new ObjectId(req.body._id) },
    {
      $set: { 
        name: req.body.name,
        description: req.body.description,
        image: req.body.image  // Add this line to update the image
      },
    }
  );
  var o_id = new ObjectId(req.body._id);
  const result = await categoryCollection.find({ _id: o_id }).toArray();
  res.send(result[0]);
});

app.post("/categories", cors(), async (req, res) => {
  await categoryCollection.insertOne(req.body);
  res.send(req.body);
});

app.delete("/categories/:id", cors(), async (req, res) => {
  var o_id = new ObjectId(req.params["id"]);
  const result = await categoryCollection.find({ _id: o_id }).toArray();
  await categoryCollection.deleteOne({ _id: o_id });
  res.send(result[0]);
});
//---------------------CUSTOMER----------------------------//


app.get("/accounts", cors(), async (req, res) => {
  const result = await accountCollection.find({}).toArray();
  res.send(result);
});

app.get("/accounts/:phonenumber", cors(), async (req, res) => {
  const phone = req.params["phonenumber"];
  const result = await accountCollection
    .find({ phonenumber: phone })
    .toArray();
  res.send(result[0]);
});
app.get("/customers", async (req, res) => {
  try {
    const result = await customerCollection.find({}).toArray();
    res.send(result);
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});

// Lấy khách hàng theo ID
app.get("/customer/:id", async (req, res) => {
  try {
    const id = req.params["id"];
    const o_id = new ObjectId(id);
    const result = await customerCollection.find({ _id: o_id }).toArray();

    if (result.length === 0) {
      return res.status(404).send({ message: "Customer not found." });
    }

    res.send(result[0]);
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});

// Lấy khách hàng theo số điện thoại
app.get("/customers/phone/:phonenumber", cors(), async (req, res) => {
  try {
    const phone = req.params["phonenumber"];

    const customer = await customerCollection.findOne({ Phone: phone });

    if (!customer) {
      return res.status(404).send({
        success: false,
        message: "Không tìm thấy khách hàng với số điện thoại này."
      });
    }
    res.status(200).send({
      success: true,
      data: customer
    });

  } catch (error) {
    console.error("Lỗi khi tìm kiếm khách hàng:", error);
    res.status(500).send({
      success: false,
      message: "Đã xảy ra lỗi khi tìm kiếm khách hàng.",
      error: error.message
    });
  }
});

// Thêm khách hàng mới
app.post("/customers", async (req, res) => {
  try {
    const newCustomer = req.body;

    if (!newCustomer.CreatedAt) {
      newCustomer.CreatedAt = new Date();
    }
    if (!newCustomer.Cart) {
      newCustomer.Cart = [];
    }

    const result = await customerCollection.insertOne(newCustomer);
    res.status(201).send({
      ...newCustomer,
      _id: result.insertedId
    });
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});

// Cập nhật thông tin khách hàng
app.put("/customers/:id", async (req, res) => {
  try {
    const id = req.params.id;
    const o_id = new ObjectId(id);
    const updates = {
      Name: req.body.Name,
      Phone: req.body.Phone,
      Mail: req.body.Mail,
      DOB: req.body.DOB,
      Address: req.body.Address,
      Gender: req.body.Gender,
      Image: req.body.Image
    };

    Object.keys(updates).forEach(key => {
      if (updates[key] === undefined) {
        delete updates[key];
      }
    });

    await customerCollection.updateOne(
      { _id: o_id },
      { $set: updates }
    );

    const updatedCustomer = await customerCollection.findOne({ _id: o_id });
    if (!updatedCustomer) {
      return res.status(404).send({ message: "Customer not found." });
    }

    res.send(updatedCustomer);
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});

// Lấy giỏ hàng khách hàng theo ID
app.get("/customers/:id/cart", async (req, res) => {
  try {
    const id = req.params.id;
    const o_id = new ObjectId(id);
    const getCart = await customerCollection.findOne({ _id: o_id });

    if (!getCart) {
      return res.status(404).send({ message: "Không tìm thấy khách hàng." });
    }

    res.send(getCart.Cart || []); // Nếu không có giỏ hàng, trả về mảng rỗng
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});

// Cập nhật giỏ hàng khách hàng
app.put("/customers/:id/cart", async (req, res) => {
  try {
    const id = req.params.id;
    const o_id = new ObjectId(id);
    const updatedCart = req.body.Cart || [];

    await customerCollection.updateOne(
      { _id: o_id },
      { $set: { Cart: updatedCart } }
    );

    const updatedCustomer = await customerCollection.findOne({ _id: o_id });
    if (!updatedCustomer) {
      return res.status(404).send({ message: "Customer not found." });
    }

    res.send(updatedCustomer);
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});

// Xóa khách hàng
app.delete("/customers/:id", async (req, res) => {
  try {
    const id = req.params.id;
    const o_id = new ObjectId(id);

    const result = await customerCollection.deleteOne({ _id: o_id });

    if (result.deletedCount === 0) {
      return res.status(404).send({ message: "Customer not found." });
    }

    res.send({ message: "Customer deleted successfully." });
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});
//Đăng ký và Đăng nhập + hàm băm
app.post("/accounts", cors(), async (req, res) => {
  var crypto = require('crypto');
  salt = crypto.randomBytes(16).toString('hex');
  userCollection = database.collection("Account");
  user = req.body;
  hash = crypto.pbkdf2Sync(user.password, salt, 1000, 64, `sha512`).toString(`hex`);
  user.password = hash;
  user.salt = salt
  await userCollection.insertOne(user)
  res.send(req.body)
})
//Đăng nhập tài khoản 
app.post('/login', cors(), async (req, res) => {
  const { phonenumber, password } = req.body;
  const crypto = require('crypto');
  const userCollection = database.collection('Account');
  const user = await userCollection.findOne({ phonenumber });
  if (user == null) {
    res.status(401).send({ message: 'Unexisted username' });
  } else {
    const hash = crypto.pbkdf2Sync(password, user.salt, 1000, 64, `sha512`).toString(`hex`);
    if (user.password === hash) {
      res.send(user);
    } else {
      res.status(401).send({ message: 'False password' });
    }
  }
});

app.put('/change-password', cors(), async (req, res) => {
  const { phonenumber, oldPassword, newPassword } = req.body;
  const crypto = require('crypto');
  const userCollection = database.collection('Account');
  const user = await userCollection.findOne({ phonenumber });
  if (user == null) {
    res.status(401).send({ message: 'Unexisted username' });
  } else {
    const oldHash = crypto.pbkdf2Sync(oldPassword, user.salt, 1000, 64, `sha512`).toString(`hex`);
    if (user.password !== oldHash) {
      res.status(401).send({ message: 'False old password' });
    } else {
      const newSalt = crypto.randomBytes(16).toString(`hex`);
      const newHash = crypto.pbkdf2Sync(newPassword, newSalt, 1000, 64, `sha512`).toString(`hex`);
      await userCollection.updateOne({ phonenumber }, { $set: { password: newHash, salt: newSalt } });
      res.send({ message: 'Change password successfully' });
    }
  }
});
app.put('/reset-password', cors(), async (req, res) => {
  const { phonenumber, newPassword, verificationCode } = req.body;
  const crypto = require('crypto');
  const userCollection = database.collection('Account');

  const user = await userCollection.findOne({ phonenumber });

  if (user == null) {
    res.status(401).send({ message: 'Unexisted username' });
  } else {
    try {


      const newSalt = crypto.randomBytes(16).toString(`hex`);
      const newHash = crypto.pbkdf2Sync(newPassword, newSalt, 1000, 64, `sha512`).toString(`hex`);

      await userCollection.updateOne(
        { phonenumber },
        { $set: { password: newHash, salt: newSalt } }
      );

      res.send({ message: 'Password has been reset successfully' });
    } catch (error) {
      console.error('Reset password error:', error);
      res.status(500).send({ message: 'Error resetting password' });
    }
  }
});

// ===== ADMIN APIS =====
// Get admin by ID
app.get("/admins/:id", cors(), async (req, res) => {
  var o_id = new ObjectId(req.params["id"]);
  const result = await adminCollection.find({ _id: o_id }).toArray();
  res.send(result[0]);
});

// Get all admins
app.get("/admins", cors(), async (req, res) => {
  const result = await adminCollection.find({}).toArray();
  res.send(result);
});

//-------------------ORDER API---------------------
app.get("/orders", async (req, res) => {
  try {
    const result = await orderCollection.find({}).toArray();
    res.status(200).send(result);
  } catch (error) {
    console.error("Error fetching products:", error);
    res.status(500).send({ error: "Error fetching products" });
  }
});
app.get("/orders/:id", cors(), async (req, res) => {
  var o_id = new ObjectId(req.params["id"]);
  const result = await orderCollection.find({ _id: o_id }).toArray();
  res.send(result[0]);
});
// Lấy đơn hàng của khách hàng theo customerId
app.get("/orders/customer/:customerId", cors(), async (req, res) => {
  try {
    const customerId = req.params["customerId"];

    // Tìm tất cả đơn hàng có customerId khớp
    const orders = await orderCollection.find({ Customer_id: new ObjectId(customerId) }).toArray();

    if (!orders || orders.length === 0) {
      return res.status(404).send({
        success: false,
        message: "Không tìm thấy đơn hàng cho khách hàng này."
      });
    }

    res.status(200).send({
      success: true,
      data: orders
    });

  } catch (error) {
    console.error("Lỗi khi lấy đơn hàng:", error);
    res.status(500).send({
      success: false,
      message: "Đã xảy ra lỗi khi lấy đơn hàng.",
      error: error.message
    });
  }
});

// API to update an order
app.put("/orders/:id", cors(), async (req, res) => {
  try {
    const { id } = req.params;  // Get order ID from request params
    const {
      Customer_id,
      Order_date,
      Shipping_address,
      Status,
      Total_price,
      Items
    } = req.body;

    // Create updated order object
    const updatedOrder = {
      Customer_id: new ObjectId(Customer_id),
      Order_date: new Date(Order_date),
      Shipping_address,
      Status,
      Total_price: parseFloat(Total_price),
      Items: Array.isArray(Items) ? Items : [], // Ensure Items is an array
    };

    // Update the order in the database
    const result = await orderCollection.updateOne(
      { _id: new ObjectId(id) },  // Find order by ID
      { $set: updatedOrder }  // Set the new values for the order
    );

    if (result.matchedCount === 0) {
      return res.status(404).send({ error: "Order not found" });
    }

    // Return the updated order details
    res.status(200).send({
      _id: id,
      ...updatedOrder
    });
  } catch (error) {
    console.error("Error updating order:", error);
    res.status(500).send({ error: "Error updating order" });
  }
});


app.post("/orders", cors(), async (req, res) => {
  //put json Order into database 
  await orderCollection.insertOne(req.body)
  //send message to client(send all database to client) 
  res.send(req.body)
})
app.delete("/orders/:id", cors(), async (req, res) => {
  //find detail Order with id 
  var o_id = new ObjectId(req.params["id"]);
  const result = await orderCollection.find({ _id: o_id }).toArray();
  //update json Order into database 
  await orderCollection.deleteOne(
    { _id: o_id }
  )
  res.send(result[0])
}) 

//--------BUY NOW API--------
// API để thêm sản phẩm vào BuyNowItems (mua ngay)
app.post("/buynow", async (req, res) => {
  const { productId, quantity } = req.body;

  try {
    const product = await productCollection.findOne({ _id: new ObjectId(productId) });
    if (!product) {
      return res.status(404).send({ error: "Product not found" });
    }

    req.session.buyNowItems = [];

    // Thêm sản phẩm vào BuyNowItems
    req.session.buyNowItems.push({
      productId,
      _id: product._id,
      category_id: product.category_id,
      Name: product.Name,
      Price: product.Price,
      Image: product.Image,
      Description: product.Description,
      Origin: product.Origin,
      Uses: product.Uses,
      Store: product.Store,
      Quantity: product.Quantity,
      cartQuantity: quantity,
      Create_date: product.Create_date,
      Dimension: product.Dimension,
      Story: product.Story,
      ProductCare: product.ProductCare,
      ShippingReturn: product.ShippingReturn
    });

    console.log("Buy Now Items updated:", req.session.buyNowItems);
    res.status(200).send(req.session.buyNowItems);
  } catch (error) {
    console.error("Error adding to Buy Now:", error);
    res.status(400).send({ error: "Invalid product ID or request" });
  }
});

// API để lấy sản phẩm từ BuyNowItems
app.get("/buynow", (req, res) => {
  console.log("Fetching Buy Now items:", req.session.buyNowItems);
  res.status(200).send(req.session.buyNowItems);
});


app.get("/customers/phone/:phone/cart", async (req, res) => {
  try {
    const phone = req.params.phone;
    // Tìm khách hàng theo số điện thoại
    const customer = await customerCollection.findOne({ Phone: phone });

    if (!customer) {
      return res.status(404).send({ message: "Không tìm thấy khách hàng với số điện thoại này." });
    }

    res.send(customer.Cart || []); // Nếu không có giỏ hàng, trả về mảng rỗng
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});



app.put("/customers/phone/:phone/cart", async (req, res) => {
  try {
    const phone = req.params.phone;
    const newCart = req.body.cart; // Nhận dữ liệu giỏ hàng mới từ request body

    if (!Array.isArray(newCart)) {
      return res.status(400).send({ message: "Dữ liệu giỏ hàng không hợp lệ. Phải là một mảng." });
    }

    // Tìm và cập nhật giỏ hàng của khách hàng
    const updatedCustomer = await customerCollection.findOneAndUpdate(
      { Phone: phone },
      { $set: { Cart: newCart } },
      { returnDocument: "after" } // Trả về tài liệu sau khi cập nhật
    );

    if (!updatedCustomer) {
      return res.status(404).send({ message: "Không tìm thấy khách hàng với số điện thoại này." });
    }
    res.send(updatedCustomer.Cart);
  } catch (error) {
    res.status(500).send({ message: error.message });
  }
});

// API tìm kiếm sản phẩm
app.get("/search", async (req, res) => {
  const query = req.query.q; // Lấy từ khóa tìm kiếm từ query parameter

  if (!query) {
    return res.status(400).send({ error: "Từ khóa tìm kiếm không hợp lệ." });
  }

  try {
    // Tìm kiếm sản phẩm trong productCollection với tên sản phẩm khớp với từ khóa
    const products = await productCollection.find({
      Name: { $regex: query, $options: 'i' } // Tìm kiếm không phân biệt chữ hoa chữ thường
    }).toArray();

    // Nếu không tìm thấy sản phẩm nào
    if (products.length === 0) {
      return res.status(404).send({ message: "Không có sản phẩm nào khớp với từ khóa." });
    }

    // Chuyển kết quả thành mảng các sản phẩm với thông tin cần thiết
    const results = products.map((product) => ({
      type: 'product',
      name: product.Name,
      price: product.Price,
      link: `/product-detail/${encodeURIComponent(product.Name.trim())}`,
      image: product.Image // Giả sử mỗi sản phẩm có ít nhất một hình ảnh
    }));

    // Trả về kết quả tìm kiếm
    res.status(200).send(results);
  } catch (error) {
    console.error("Lỗi khi tìm kiếm sản phẩm:", error);
    res.status(500).send({ error: "Lỗi khi lấy kết quả tìm kiếm" });
  }
});
