package app;

import infra.db.DbSeeder;
import infra.db.InitDb;

public class SeedDbMain {
    public static void main(String[] args) {
        try {
            // 1) Create tables first (safe to re-run)
            InitDb.createTables();

            // 2) Populate database using the CSV under resources
            DbSeeder.seedRoutesFromCsv("./resources/eu_rail_network.csv");

            System.out.println("Database ready!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}