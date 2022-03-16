# mattclarke.dev

A personal blog by Matthew Clarke.

## Design Specification

### Users

- Myself, a designer, developer, and writer. 
- Hiring Professionals, either technical requiters, managers, designers, developers and product managers.
- Non-hiring Professionals, either hackers, designers, with a range of skillsets

### Use Cases

- Editing and publishing, I can trivially edit and publish media-rich articles
- Reading and viewing media in order to assess my capabilities and how I think about managing products
- Reading and viewing media in order to learn something new/interesting

### Requirements

- Consistent, trival publishing, I can write and publish independent of coding
- Low latency loads, users around the world can enter and exit pages near to local read speeds
- No jank, jitter, or glitch, users shoudn't become aware they're using a website mid-experience
- Navigation, users can go from article to article without thinking

### Device Support

- XL desktop, desktop, tablet, mobile
- Decent browser support (ES3)

### Technical Specification

In a functional spirit, we're going to avoid state and javascript events by default.
Our program should be a function that accepts a collection of markdown files and outputs a multi-page website (HTML, CSS, JS) to be statically hosted.

- Input data: Articles written in markdown, and media assets
- Output data: HTML, CSS, JS, media assets

#### Parsing

- Markdown parsing to HTML
  - https://github.com/yogthos/markdown-clj