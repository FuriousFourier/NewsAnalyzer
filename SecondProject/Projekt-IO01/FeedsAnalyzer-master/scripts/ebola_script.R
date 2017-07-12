library(plotly)

file <- "../data/ebolaDate.csv"
output_folder <- "../plots/plots_ebola/"

data <- read.csv(file, header = FALSE, sep = "\t")



p <- plot_ly(
  x = as.Date(data$V1),
  y = data$V2,
  type = "bar",
  text = text
) %>%
layout(
  title = "Liczba wspomnień o eboli w artykułach w zależności od czasu",
  xaxis = list(title = ""),
  yaxis = list(title = ""),
  margin = list(b = 120)
)

export(p, file=paste(output_folder, "ebola_date", ".pdf", sep = ""))
