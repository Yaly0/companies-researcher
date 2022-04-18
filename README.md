# companies-researcher
This project is a simple google search scraper that works as follows:
1. User fills "companies.txt" file with companies/organisations/people/etc. they want to research about.
2. User types in the code terms they are looking for, e.g. they want to see which companies are British 
   or owned by British companies, so the terms wound be: "UK", "British", "United Kingdom", "Britain".
3. User types in the code terms used by google search for further research, e.g. parent companies, spouse, etc., 
   for the example described in 2., the terms would be: "Parent organization", "Owner".
4. The program filters the list by desired conditions until the end of the list or until the google search request rate limit is reached.
5. While running, the program writes the filtered content into "filtered_companies.txt" file, and checked content in "checked_companies".
6. If the program is interrupted because the rate limit is reached, the user removes checked companies from the companies file,
   changes VPN or waits and runs the program again.
   
This project doesn't filter the content 100% as desired due to its simplicity.

One of the important features is that the program can't go into an infinite loop because of connected results, 
e.g. company A owns part of company B, and company B owns part of company A, another example is people with their spouses.
