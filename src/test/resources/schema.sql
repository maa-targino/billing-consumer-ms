create table if not exists customers (
    customer_id serial primary key,
    company_name text not null,
    contact_name text,
    email text not null,
    billing_address text,
    shipping_address text
);

create table if not exists products_services (
    product_id serial primary key,
    name text not null,
    description text,
    unit_price numeric(10, 2) not null,
    tax_rate numeric(5, 2) not null
);

create table if not exists invoices (
    invoice_id serial primary key,
    invoice_number varchar(50) not null unique,
    customer_id integer not null references customers (customer_id),
    issue_date date not null,
    due_date date not null,
    status varchar(50) not null,
    notes text
);

create table if not exists invoice_line_items (
    line_item_id serial primary key,
    invoice_id integer not null references invoices (invoice_id),
    product_id integer not null references products_services (product_id),
    quantity numeric(10, 2) not null,
    historic_unit_price numeric(10, 2) not null,
    discount_amount numeric(10, 2),
    tax_amount numeric(10, 2),
    line_total numeric(10, 2) not null
);
