- [SPAGHETTI](#spaghetti)
- [SPAGHETTI Generator](#spaghetti-generator)
- [Illustration](#illustration)
  - [Loop Tiling vs Pattern-Aware Tiling](#loop-tiling-vs-pattern-aware-tiling)
- [Experimental Results](#experimental-results)
  - [Density of Input and Output Matrix](#density-of-input-and-output-matrix)
  - [Normalize Exe time. SpArch to Stall-free Ideal SpArch.](#normalize-exe-time-sparch-to-stall-free-ideal-sparch)
- [Bandwidth Utilization of SpArch.](#bandwidth-utilization-of-sparch)

## SPAGHETTI

Sparse--Sparse matrix multiplications are widely used across multiple domains, but the regularity of the computation is dependent on the input sparsity pattern. The majority of recent hardware accelerators are based on the inner-product method and propose new storage formats to regularize the computation. We find that these storage formats are more suited for denser matrices and inherently suffer from high DRAM accesses for sparse matrices (<1% density). Some works have adopted the outer-product algorithm which targets matrices stored in CSR/CSC format and thus are better suited for highly sparse matrices. State-of-the-art outer-product accelerators condense inputs to improve output reuse, but then spoil input reuse, requiring a complex memory hierarchy (e.g., prefetch caches). The condensing effectiveness also varies across inputs (and even row-to-row within an input) which leads to high variance in DRAM utilization and speedup across inputs.

We propose SPAGHETTI, an open-source Chisel generator for creating FPGA-optimized sparse GEMM accelerators. The key novelty in Spaghetti is a new pattern-aware software scheduler that analyzes the sparsity pattern and schedules row-col pairs of the inputs onto the fixed microarchitecture. Spaghetti takes advantage of our observation that in outer-product the rows in the input matrix lead to mutually independent rows in the final output. Thus the scheduler can partition the input into tiles that maximize reuse and eliminate re-fetching of the partial matrices from the DRAM. The microarchitecture template we create has the following key benefits i) we can statically schedule the inputs in a streaming fashion and maximize DRAM utilization, ii) we can parallelize the merge phase and generate multiple rows of the output in parallel maximally using the output DRAM bandwidth, iii) we can adapt to the varying logic resources and bandwidth across various FPGA boards and attain maximal roofline performance (only limited by memory-bandwidth). We auto-generate GEMM accelerators on Amazon AWS FPGAs and demonstrate that we can achieve performance improvement over CPUs and GPUs between 1.1--34.5x. For highly sparse inputs compared to state-of-the-art outer-product accelerators, we improved performance on average 2.6x, and reduce DRAM accesses on average 4x compared to the state-of-the-art outer product accelerator.

## SPAGHETTI Generator

The \SPAGHETTI{} generator has been implemented as a Chisel library. It enables parameterization and configurability through a set of high-level meta-programming and functional programming abstractions. \SPAGHETTI{} generator hides all the implementation details of the microarchitecture and exposes a simple architectural model that the designer can tune. Figure~\ref{fig:system} shows two major classes in the generator: i) the parameters class that allows the designer to set the different tunable parameters listed in Section~\ref{sec:scheduling} and ii) the top-level \SPAGHETTI{} accelerator class, including the IO interface and design modules.
\SPAGHETTI{} generator supports different operand types for each module (Green types). We support fully customizable integer, fixed point, and floating point formats. The memory module (Red module) contains a configurable AXI bus interface. The parametric design of the generator enables the accelerator to support different configurations of the memory interfaces to support different FPGA families, in this example we have included AWS configuration parameters. Different FPGA boards support varied AXI interfaces e.g., AWS FPGA supports 4 512 bit channels, while Altera Cyclone-V supports 1 64-bit channel.

![Accelerator](https://www.dropbox.com/s/zwbivi3zkvomd83/Generator.png?raw=1)

## Illustration

### Loop Tiling vs Pattern-Aware Tiling

## Experimental Results

### Density of Input and Output Matrix

![Accelerator](https://www.dropbox.com/s/hz074ot3zg3lpcq/Density.png?raw=1)

### Normalize Exe time. SpArch to Stall-free Ideal SpArch.

![Accelerator](https://www.dropbox.com/s/geehpln5w5s4qte/Serialization.png?raw=1)

## Bandwidth Utilization of SpArch.

![Accelerator](https://www.dropbox.com/s/z67a7w9hwg5qa3l/SpArch_BW.png?raw=1)
