import pandas as pd
import numpy as np
import sys

import plotly.express as px

def main():
    file_name = sys.argv[1]
    df = pd.read_csv(file_name)
    fig = px.line(df, x='MessageIndex', y='Latency(us)', title='Latency Scatter Plot')
    fig.show()

if __name__ == "__main__":
    main()